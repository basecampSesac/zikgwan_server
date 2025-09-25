package basecamp.zikgwan.ticketsale;

import basecamp.zikgwan.common.domain.BaseEntity;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.ticketsale.enums.Seat;
import basecamp.zikgwan.ticketsale.enums.TicketState;
import basecamp.zikgwan.user.User;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
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

    @Column(name = "ticket_cnt", nullable = false)
    private Integer ticketCnt;

    @Column(name = "home", length = 50, nullable = false)
    private String home;

    @Column(name = "away", length = 50, nullable = false)
    private String away;

    @Column(name = "stadium", length = 2, nullable = false)
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

    // 사용자와 N:1 관계
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_ud", nullable = false)
    private User user;

    public void setUser(User user) {
        this.user = user;
    }

}
