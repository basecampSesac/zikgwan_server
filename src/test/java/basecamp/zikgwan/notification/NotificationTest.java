package basecamp.zikgwan.notification;

import static org.assertj.core.api.Assertions.assertThat;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.user.domain.User;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class NotificationTest {

    @DisplayName("알림을 읽음 처리한다.")
    @Test
    void updateReadAt() {
        // given
        User user = User.builder()
                .userId(1L)
                .nickname("nick")
                .email("test@test.com")
                .password("@test1234")
                .club(null)
                .build();

        Notification notification = Notification.builder()
                .roomId(1L)
                .message("알림 메시지")
                .readAt(null)
                .receiver(user)
                .build();

        LocalDateTime readAt = LocalDateTime.now();

        // when
        notification.updateReadAt(readAt);

        // then
        assertThat(notification.getReadAt()).isEqualTo(readAt);
    }

    @DisplayName("알림을 삭제한다.")
    @Test
    void updateSaveState() {
        // given
        User user = User.builder()
                .userId(1L)
                .nickname("nick")
                .email("test@test.com")
                .password("@test1234")
                .club(null)
                .build();

        Notification notification = Notification.builder()
                .roomId(1L)
                .message("알림 메시지")
                .readAt(null)
                .receiver(user)
                .build();

        SaveState state = SaveState.N;

        // when
        notification.updateSaveState(state);

        // then
        assertThat(notification.getSaveState()).isEqualTo(state);
    }

}