package basecamp.zikgwan.review;

import basecamp.zikgwan.common.domain.CreatedEntity;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.ticketsale.TicketSale;
import basecamp.zikgwan.user.domain.User;
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
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "review")
public class Review extends CreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Long reviewId;

    // 거래 1건당 리뷰 1개 (1:1 관계)
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "ts_id", nullable = false, unique = true)
    private TicketSale ticketSale;

    @Column(name = "rating", nullable = false)
    private Double rating;

    // 평가한 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private User reviewer;

    // 평가 받은 회원
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private User reviewee;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Y'")
    @Column(name = "save_state", nullable = false)
    private SaveState saveState;

    public void setReviewer(User reviewer) {
        this.reviewer = reviewer;
    }

    public void setReviewee(User reviewee) {
        this.reviewee = reviewee;
    }

    @Builder
    private Review(Long reviewId, TicketSale ticketSale, Double rating, User reviewer, User reviewee) {
        this.reviewId = reviewId;
        this.ticketSale = ticketSale;
        this.rating = rating;
        this.reviewer = reviewer;
        this.reviewee = reviewee;
        this.saveState = SaveState.Y;
    }
}
