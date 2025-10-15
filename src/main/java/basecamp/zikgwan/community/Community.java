package basecamp.zikgwan.community;

import basecamp.zikgwan.common.domain.BaseEntity;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.community.dto.CommunityRequest;
import basecamp.zikgwan.community.enums.CommunityState;
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
import lombok.Builder;
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

    @Column(name = "date", nullable = false)
    private LocalDateTime date;

    @Column(name = "stadium", length = 100)
    private String stadium;

    @Column(name = "home_team", length = 100)
    private String home;

    @Column(name = "away_team", length = 100)
    private String away;

    @Column(name = "member_count", nullable = false)
    @ColumnDefault("'0'")
    private Integer memberCount;

    // 채팅방 참여 가능 여부
    @Column(name = "is_full", nullable = false)
    @ColumnDefault("false")
    private Boolean isFull = false;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'ING'")
    @Column(name = "state", length = 3, nullable = false)
    private CommunityState state;//모임 상태(ING- 진행중, END-완료)

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Y'")
    @Column(name = "save_state", nullable = false)
    private SaveState saveState;

    // 모임장
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "leader_id", nullable = false)
    private User user;

    public void setUser(User user) {
        this.user = user;
    }

    @Builder
    private Community(String title, String description, LocalDateTime date, String stadium, String home, String away,
                      Integer memberCount, Boolean isFull, User user) {
        this.title = title;
        this.description = description;
        this.date = date;
        this.stadium = stadium;
        this.home = home;
        this.away = away;
        this.memberCount = memberCount;
        this.isFull = isFull;
        this.user = user;
        this.saveState = SaveState.Y;
        this.state = CommunityState.ING;
    }

    public void updateCommunity(CommunityRequest request) {
        this.title = request.getTitle();
        this.description = request.getDescription();
        this.date = request.getDate();
        this.stadium = request.getStadium();
        this.home = request.getHome();
        this.away = request.getAway();
        this.memberCount = request.getMemberCount();
    }

    public void updateIsFull(Boolean full) {
        isFull = full;
    }

    //저장상태변경
    public void setSaveState(SaveState saveState) {
        this.saveState = saveState;
    }

    //모임상태변경
    public void setState(CommunityState state) {
        this.state = state;
    }
}
