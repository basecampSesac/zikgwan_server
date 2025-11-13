package basecamp.zikgwan.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatRoomDetailDto {

    private Long roomId;
    private String roomName;
    private Integer userCount;
}
