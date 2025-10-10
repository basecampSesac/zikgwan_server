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
public class EventPayload {

    // TODO nickname, createAt 추가
    private Long roomId;

    private String message;

    private String nickname;

    private LocalDateTime sentAt;

}
