package basecamp.zikgwan.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketRoomCount {

    private Long tsId;  // 티켓 거래글 id
    private Long chatRoomCount; // 해당 거래글의 채팅방 개수
}
