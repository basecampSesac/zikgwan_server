package basecamp.zikgwan.notification.dto;

import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationResponseDto {

    private Long id;
    private Long roomId;
    private String message;
    private LocalDateTime readAt;
    private String senderNickname;  // 보낸 사람 닉네임
    private LocalDateTime sentAt;   // 보낸 시각
}
