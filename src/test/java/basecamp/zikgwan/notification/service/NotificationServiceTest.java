package basecamp.zikgwan.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.notification.Notification;
import basecamp.zikgwan.notification.dto.NotificationResponseDto;
import basecamp.zikgwan.notification.repository.NotificationRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    NotificationService notificationService;

    @Mock
    NotificationRepository notificationRepository;

    @Mock
    UserRepository userRepository;

    @DisplayName("사용자의 모든 알림을 조회한다.")
    @Test
    void getNotifications() {
        // given
        User user1 = createUser(1L, "user1", "user1@email.com", SaveState.Y);

        Notification notification1 = createNotification(1L, "알림1", null, user1);
        Notification notification2 = createNotification(2L, "알림2", LocalDateTime.now().minusHours(2), user1);
        Notification notification3 = createNotification(3L, "알림3", null, user1);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(user1));

        given(notificationRepository.findAllByReceiverAndSaveState(eq(user1), eq(SaveState.Y)))
                .willReturn(List.of(notification1, notification2, notification3));

        // when
        List<NotificationResponseDto> notifications = notificationService.getNotifications(1L);

        // then

        assertThat(notifications).hasSize(3)
                .extracting("roomId", "message")
                .containsExactlyInAnyOrder(
                        tuple(1L, "알림1"),
                        tuple(2L, "알림2"),
                        tuple(3L, "알림3")
                );

        then(userRepository).should(times(1)).findById(1L);
        then(notificationRepository).should().findAllByReceiverAndSaveState(eq(user1), eq(SaveState.Y));

    }

    @DisplayName("사용자의 모든 알림을 조회 시 존재하지 않는 사용자의 경우 예외를 던진다.")
    @Test
    void getNotificationsWithWrongUserId() {
        // given

        Long wrongUserId = 222L;

        given(userRepository.findById(eq(wrongUserId))).willReturn(Optional.empty());

        // when

        // then

        assertThatThrownBy(() -> notificationService.getNotifications(wrongUserId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("사용자가 존재하지 않습니다.");

        then(userRepository).should(times(1)).findById(wrongUserId);
        then(notificationRepository).shouldHaveNoInteractions();

    }

    @DisplayName("알림을 읽음 처리할 수 있다.")
    @Test
    void readNotification() {
        // given
        User user1 = createUser(1L, "user1", "user1@email.com", SaveState.Y);
        Notification notification1 = createNotification(1L, "알림1", null, user1);

        given(notificationRepository.findById(anyLong())).willReturn(Optional.of(notification1));

        // when
        String message = notificationService.readNotification(1L, 1L);

        // then
        assertThat(message).isNotEmpty()
                .isEqualTo(notification1.getMessage() + " 알림 읽음 처리 완료");

        then(notificationRepository).should(times(1)).findById(1L);
        then(notificationRepository).should(times(1)).save(notification1);
    }

    @DisplayName("알림 읽음 처리 시 알림이 존재하지 않으면 예외가 발생한다.")
    @Test
    void readNotificationWithWrongNotificationId() {
        // given
        Long wrongNotificationId = 222L;

        given(notificationRepository.findById(eq(wrongNotificationId))).willReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> notificationService.readNotification(123L, wrongNotificationId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("알림이 존재하지 않습니다.");

        then(notificationRepository).should(times(1)).findById(wrongNotificationId);
    }

    @DisplayName("알림 읽음 처리 시 다른 사용자의 알림이면 예외를 던진다.")
    @Test
    void readNotificationWithWrongUserId() {
        // given
        User user1 = createUser(1L, "user1", "user1@email.com", SaveState.Y);
        User user2 = createUser(2L, "user2", "user2@email.com", SaveState.Y);

        Notification notification1 = createNotification(1L, "알림1", null, user1);

        given(notificationRepository.findById(eq(1L)))
                .willReturn(Optional.of(notification1));

        // when

        // then

        assertThatThrownBy(() -> notificationService.readNotification(user2.getUserId(), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 유저의 알림이 아닙니다.");

        then(notificationRepository).should(times(1)).findById(eq(1L));
        then(notificationRepository).should(never()).save(any());
    }

    @DisplayName("사용자의 알림을 삭제한다.")
    @Test
    void deleteNotification() {
        // given
        User user1 = createUser(1L, "user1", "user1@email.com", SaveState.Y);

        Notification notification1 = createNotification(1L, "알림1", null, user1);

        given(notificationRepository.findById(1L))
                .willReturn(Optional.of(notification1));

        // when
        String message = notificationService.deleteNotification(1L, 1L);

        // then
        assertThat(message).isEqualTo(notification1.getMessage() + " 알림 삭제 완료");

        then(notificationRepository).should(times(1)).findById(1L);
        then(notificationRepository).should(times(1)).save(notification1);
    }

    @DisplayName("알림 삭제 시 알림이 존재하지 않으면 예외가 발생한다.")
    @Test
    void deleteNotificationWithWrongNotificationId() {
        // given
        Long wrongNotificationId = 222L;

        given(notificationRepository.findById(eq(wrongNotificationId))).willReturn(Optional.empty());

        // when

        // then
        assertThatThrownBy(() -> notificationService.deleteNotification(123L, wrongNotificationId))
                .isInstanceOf(NoSuchElementException.class)
                .hasMessageContaining("알림이 존재하지 않습니다.");

        then(notificationRepository).should(times(1)).findById(wrongNotificationId);
    }

    @DisplayName("알림 삭제 시 다른 사용자의 알림이면 예외를 던진다.")
    @Test
    void deleteNotificationWithWrongUserId() {
        // given
        User user1 = createUser(1L, "user1", "user1@email.com", SaveState.Y);
        User user2 = createUser(2L, "user2", "user2@email.com", SaveState.Y);

        Notification notification1 = createNotification(1L, "알림1", null, user1);

        given(notificationRepository.findById(eq(1L)))
                .willReturn(Optional.of(notification1));

        // when

        // then

        assertThatThrownBy(() -> notificationService.deleteNotification(user2.getUserId(), 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("해당 유저의 알림이 아닙니다.");

        then(notificationRepository).should(times(1)).findById(eq(1L));
        then(notificationRepository).should(never()).save(any());
    }


    private User createUser(Long userId, String nickname, String email, SaveState saveState) {
        return User.builder()
                .userId(userId)
                .nickname(nickname)
                .email(email)
                .saveState(saveState)
                .build();
    }

    private Notification createNotification(Long roomId, String message, LocalDateTime readAt, User user) {
        return Notification.builder()
                .receiver(user)
                .roomId(roomId)
                .readAt(readAt)   // 기본값 null (안 읽음)
                .message(message)
                .build();
    }

}