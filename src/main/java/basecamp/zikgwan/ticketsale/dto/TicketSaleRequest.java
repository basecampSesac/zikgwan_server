package basecamp.zikgwan.ticketsale.dto;

import basecamp.zikgwan.ticketsale.enums.Seat;
import basecamp.zikgwan.ticketsale.enums.TicketState;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSaleRequest {

    private String title; // 티켓 거래글 제목
    private String description; // 티켓 거래글 설명
    private Integer price; // 티켓 가격
    private LocalDateTime gameDay; // 경기 일자
    private Integer ticketCount; // 티켓 매수
    private String home; // 홈 팀
    private String away; // 어웨이 팀
    private String stadium; // 야구구장 약어
    private Seat adjacentSeat; // 연석 여부
    private TicketState state; // 티켓 거래글 상태 (Default ING)
//  private List<ImageDto> images; // 티켓 게시글 이미지

}
