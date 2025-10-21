package basecamp.zikgwan.chat.dto;

import basecamp.zikgwan.chat.enums.RoomType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificationChatRoomDto {

    private Long roomId;
    private String roomName;
}
