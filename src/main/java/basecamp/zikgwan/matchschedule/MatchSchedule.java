package basecamp.zikgwan.matchschedule;

import basecamp.zikgwan.common.domain.CreatedEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "match_schedule")
public class MatchSchedule extends CreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "schedule_id")
    private Long scheduleId;

    @Column(name = "match_date", nullable = false)
    private LocalDate matchDate;

    @Column(name = "home_team", length = 3, nullable = false)
    private String homeTeam;

    @Column(name = "away_team", length = 3, nullable = false)
    private String awayTeam;

    @Column(name = "stadium", length = 10, nullable = false)
    private String stadium;

    // 엔티티에서 Builder가 필요할 때 필요한 부분만 생성자로 생성하고 @Builder 어노테이션 붙임
    @Builder
    public MatchSchedule(LocalDate matchDate, String homeTeam, String awayTeam, String stadium) {
        this.matchDate = matchDate;
        this.homeTeam = homeTeam;
        this.awayTeam = awayTeam;
        this.stadium = stadium;
    }

    // Dirty Checking을 통한 업데이트를 위한 메서드
    // 경기장 확정될 경우 업데이트
    public void updateStadium(String stadium) {
        this.stadium = stadium;
    }
}
