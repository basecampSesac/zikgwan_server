package basecamp.zikgwan.matchschedule.repository;

import basecamp.zikgwan.matchschedule.MatchSchedule;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchScheduleRepository extends JpaRepository<MatchSchedule, Long> {
    List<MatchSchedule> findAllByMatchDate(LocalDate matchDate);

    Optional<MatchSchedule> findByMatchDateAndHomeTeamAndAwayTeam(LocalDate matchDate, String homeTeam, String awayTeam);
}
