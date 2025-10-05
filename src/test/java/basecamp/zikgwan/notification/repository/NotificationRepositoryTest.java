package basecamp.zikgwan.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.notification.Notification;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class NotificationRepositoryTest {

    @Autowired
    private NotificationRepository notificationRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("사용자 정보와 저장상태로 삭제되지 않은 모든 알림을 불러온다.")
    @Test
    void findAllByReceiverAndSaveState() {
        // given
        User user = createUser("user", "user@email.com", "user1234", "club", SaveState.Y);

        userRepository.save(user);

        Notification message1 = createNotification(1L, "message1", null, user);
        Notification message2 = createNotification(2L, "message2", LocalDateTime.now(), user);
        Notification message3 = createNotification(3L, "message3", null, user);
        message1.updateSaveState(SaveState.Y);
        message2.updateSaveState(SaveState.N);
        message3.updateSaveState(SaveState.Y);

        notificationRepository.saveAll(List.of(message1, message2, message3));

        // when
        List<Notification> notifications = notificationRepository.findAllByReceiverAndSaveState(user,
                SaveState.Y);

        // then
        assertThat(notifications).hasSize(2)
                .extracting("message", "readAt", "receiver")
                .containsExactlyInAnyOrder(
                        tuple("message1", null, user),
                        tuple("message3", null, user)
                );

    }

    @DisplayName("알림이 존재하지 않는 경우 알림이 조회되지 않는다.")
    @Test
    void findAllByReceiverAndSaveStateWithoutMessage() {
        // given
        User user = createUser("user", "user@email.com", "user1234", "club", SaveState.Y);

        userRepository.save(user);

        // when
        List<Notification> notifications = notificationRepository.findAllByReceiverAndSaveState(user,
                SaveState.Y);

        // then
        assertThat(notifications).isEmpty();

    }

    private User createUser(String nickname, String email, String password, String club,
                            SaveState saveState) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .password(password)
                .club(club)
                .saveState(SaveState.Y)
                .build();

    }

    private Notification createNotification(Long roomId, String message, LocalDateTime readAt, User receiver) {
        return Notification.builder()
                .roomId(roomId)
                .message(message)
                .readAt(readAt)
                .receiver(receiver)
                .build();
    }

}