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

}
