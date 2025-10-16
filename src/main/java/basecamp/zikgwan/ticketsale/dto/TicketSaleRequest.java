package basecamp.zikgwan.ticketsale.dto;

import basecamp.zikgwan.ticketsale.enums.Seat;
import basecamp.zikgwan.ticketsale.enums.TicketState;
import com.fasterxml.jackson.annotation.JsonFormat;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSaleRequest {

    private String title; // 티켓 거래글 제목

    private String description; // 티켓 거래글 설명

    private Integer price; // 티켓 가격

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    @DateTimeFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime gameDay; // 경기 일자

    private Integer ticketCount; // 티켓 매수

    private String home; // 홈 팀

    private String away; // 어웨이 팀

    private String stadium; // 야구구장 약어

    private Seat adjacentSeat; // 연석 여부

    private TicketState state; // 티켓 거래글 상태 (Default ING)

}
