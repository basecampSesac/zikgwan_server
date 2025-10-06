package basecamp.zikgwan.community;

import basecamp.zikgwan.common.domain.BaseEntity;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.community.enums.CommunityState;
import basecamp.zikgwan.matchschedule.MatchSchedule;
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
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "community")
public class Community extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "community_id")
    private Long communityId;

    @Column(name = "title", length = 100, nullable = false)
    private String title;

    @Column(name = "description", nullable = true, columnDefinition = "TEXT")
    private String description;

    @Column(name = "member_count", nullable = false)
    @ColumnDefault("'0'")
    private Integer memberCount;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ING'")
    @Column(name = "state", length = 3, nullable = false)
    private CommunityState state;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Y'")
    @Column(name = "save_state", nullable = false)
    private SaveState saveState;

    // 경기 일정
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "schedule_id", nullable = false)
    private MatchSchedule matchSchedule;

    // 모임장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private User leader;

    public void setLeader(User leader) {
        this.leader = leader;
    }

}
