package basecamp.zikgwan.matchschedule.repository;

import basecamp.zikgwan.matchschedule.MatchSchedule;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MatchScheduleRepository extends JpaRepository<MatchSchedule, Long> {
}
