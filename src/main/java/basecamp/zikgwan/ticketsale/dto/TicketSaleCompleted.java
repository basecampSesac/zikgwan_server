package basecamp.zikgwan.ticketsale.dto;

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
    private String home;
    private String away;
    private String stadium;
    private String state;
    private Long sellerId;
    private Long buyerId;
    private LocalDateTime updatedAt;
    private Double rating;          // 리뷰 점수

    public static TicketSaleCompleted from(TicketSale ticketSale) {

        return TicketSaleCompleted.builder()
                .tsId(ticketSale.getTsId())
                .title(ticketSale.getTitle())
                .price(ticketSale.getPrice())
                .home(ticketSale.getHome())
                .away(ticketSale.getAway())
                .stadium(ticketSale.getStadium())
                .state(ticketSale.getState().name())
                .sellerId(ticketSale.getSellerId().getUserId())
                .buyerId(ticketSale.getBuyerId())
                .updatedAt(ticketSale.getUpdatedAt())
                .rating(ticketSale.getReview().getRating())
                .build();
    }
}