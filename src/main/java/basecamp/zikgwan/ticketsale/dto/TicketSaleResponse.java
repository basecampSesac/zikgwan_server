package basecamp.zikgwan.ticketsale.dto;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.ticketsale.TicketSale;
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
public class TicketSaleResponse {
    private Long tsId; // 티켓 거래글 id
    private String title; // 티켓 거래글 제목
    private String description; // 티켓 거래글 설명
    private Integer price; // 티켓 가격
    private LocalDateTime gameDay; // 경기 일자
    private Integer ticketCount; // 티켓 매수
    private String home; // 홈 팀
    private String away; // 어웨이 팀
    private String stadium; // 야구 구장 약어
    private Seat adjacentSeat; // 연석 여부
    private String nickname; // 티켓 거래글 작성자 닉네임
    private Double rating; // 티켓 거래글 작성자 평점
    private String profileImageUrl; // 판매자 프로필 이미지
    private TicketState state; // 티켓 거래 상태 여부
    private LocalDateTime createdAt; // 티켓 거래글 작성일자
    private LocalDateTime updatedAt; // 티켓 거래글 수정일자
    private String imageUrl;
    private SaveState saveState;

    public static TicketSaleResponse from(TicketSale ticketSale, String imageUrl, Double rating, String profileImageUrl) {
        return TicketSaleResponse.builder()
                .tsId(ticketSale.getTsId())
                .title(ticketSale.getTitle())
                .description(ticketSale.getDescription())
                .price(ticketSale.getPrice())
                .gameDay(ticketSale.getGameDay())
                .ticketCount(ticketSale.getTicketCount())
                .home(ticketSale.getHome())
                .away(ticketSale.getAway())
                .stadium(ticketSale.getStadium())
                .adjacentSeat(ticketSale.getAdjacentSeat())
                .nickname(ticketSale.getSellerId().getNickname())
                .state(ticketSale.getState())
                .imageUrl(imageUrl)
                .rating(rating)
                .profileImageUrl(profileImageUrl)
                .createdAt(ticketSale.getCreatedAt())
                .saveState(ticketSale.getSaveState())
                .build();
    }
}