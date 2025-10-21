package basecamp.zikgwan.chat.dto;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.ticketsale.enums.Seat;
import basecamp.zikgwan.ticketsale.enums.TicketState;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TicketInfoDto {

    private Long tsId;

    private String title;

    private String description;

    private Integer price;

    private LocalDateTime gameDay;

    private Integer ticketCount;

    private String home;

    private String away;

    private String stadium;

    private Seat adjacentSeat;

    private TicketState state;

    private SaveState saveState;

    private String imageUrl;

    private Long chatRoomCount;

}
