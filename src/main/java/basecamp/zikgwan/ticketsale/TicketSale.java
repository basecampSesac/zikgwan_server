package basecamp.zikgwan.ticketsale;

import basecamp.zikgwan.common.domain.BaseEntity;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.review.Review;
import basecamp.zikgwan.ticketsale.dto.TicketSaleRequest;
import basecamp.zikgwan.ticketsale.enums.Seat;
import basecamp.zikgwan.ticketsale.enums.TicketState;
import basecamp.zikgwan.user.domain.User;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.Check;
import org.hibernate.annotations.ColumnDefault;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Check(constraints = "price >= 0")
@Table(name = "ticket_sales")
public class TicketSale extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "ts_id")
    private Long tsId;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "price", nullable = false)
    private Integer price;

    @Column(name = "game_day", nullable = false)
    private LocalDateTime gameDay;

    @Column(name = "ticket_count", nullable = false)
    private Integer ticketCount;

    @Column(name = "home", length = 50, nullable = false)
    private String home;

    @Column(name = "away", length = 50, nullable = false)
    private String away;

    @Column(name = "stadium", length = 100, nullable = false)
    private String stadium;

    @Enumerated(EnumType.STRING)
    @Column(name = "adjacent_seat", length = 1, nullable = false)
    private Seat adjacentSeat;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ING'")
    @Column(name = "state", length = 3, nullable = false)
    private TicketState state;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Y'")
    @Column(name = "save_state", nullable = false)
    private SaveState saveState;

    @Builder
    private TicketSale(Long tsId, String title, String description, Integer price, LocalDateTime gameDay,
                       Integer ticketCount,
                       String home, String away, String stadium, Seat adjacentSeat, TicketState state,
                       User sellerId) {
        this.tsId = tsId;
        this.title = title;
        this.description = description;
        this.price = price;
        this.gameDay = gameDay;
        this.ticketCount = ticketCount;
        this.home = home;
        this.away = away;
        this.stadium = stadium;
        this.adjacentSeat = adjacentSeat;
        this.state = state;
        this.saveState = SaveState.Y;
        this.sellerId = sellerId;
    }

    // 티켓 판매글 수정 관련 Setter
    public void updateTicketSale(TicketSaleRequest dto) {
        this.title = dto.getTitle();
        this.description = dto.getDescription();
        this.price = dto.getPrice();
        this.gameDay = dto.getGameDay();
        this.ticketCount = dto.getTicketCount();
        this.home = dto.getHome();
        this.away = dto.getAway();
        this.stadium = dto.getStadium();
        this.adjacentSeat = dto.getAdjacentSeat();
        this.state = dto.getState();
    }

    // soft delete (게시글 저장 상태 변경)
    public void updateSaveState(SaveState saveState) {
        this.saveState = saveState;
    }


    // 판매자와 N:1 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private User sellerId;

    // 리뷰와 1:1 관계
    @OneToOne(mappedBy = "ticketSale", cascade = CascadeType.ALL, orphanRemoval = true)
    private Review review;

    // 연관관계 맺으면 구매자 지정 필수가 됨
    // 구매자와 N:1 관계
//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "buyer_id")
    @Column(name = "buyer_id")
    private Long buyerId;

    public void setSeller(User user) {
        this.sellerId = user;
    }

    // 구매자 지정
    public void updateBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    //거래글 상태변경
    public void setState(TicketState state) {
        this.state = state;
    }

    public void setBuyer(User user) {
        this.buyerId = user.getUserId();
    }
}
