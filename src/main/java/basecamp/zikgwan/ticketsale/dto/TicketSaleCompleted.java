package basecamp.zikgwan.ticketsale.dto;

import basecamp.zikgwan.review.Review;
import basecamp.zikgwan.ticketsale.TicketSale;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class TicketSaleCompleted {
    private Long tsId;
    private String title;
    private Integer price;
    private LocalDateTime gameDay; // 경기 일자
    private String home;
    private String away;
    private String stadium;
    private String state;
    private String sellerNickname;
    private Long sellerId;
    private Long buyerId;
    private LocalDateTime updatedAt;
    private Double rating;          // 리뷰 점수
    private String profileImageUrl;

    public static TicketSaleCompleted from(TicketSale ticketSale, String profileImageUrl) {

        Review review = ticketSale.getReview();

        return TicketSaleCompleted.builder()
                .tsId(ticketSale.getTsId())
                .title(ticketSale.getTitle())
                .price(ticketSale.getPrice())
                .gameDay(ticketSale.getGameDay())
                .home(ticketSale.getHome())
                .away(ticketSale.getAway())
                .stadium(ticketSale.getStadium())
                .state(ticketSale.getState().name())
                .sellerNickname(ticketSale.getSellerId().getNickname())
                .sellerId(ticketSale.getSellerId().getUserId())
                .buyerId(ticketSale.getBuyerId())
                .updatedAt(ticketSale.getUpdatedAt())
                .rating(review == null ? null : review.getRating())
                .profileImageUrl(profileImageUrl)
                .build();
    }
}