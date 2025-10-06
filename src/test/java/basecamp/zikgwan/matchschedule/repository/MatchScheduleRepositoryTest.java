package basecamp.zikgwan.matchschedule.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import basecamp.zikgwan.matchschedule.MatchSchedule;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@DataJpaTest
@ActiveProfiles("test")
@Transactional
class MatchScheduleRepositoryTest {

    @Autowired
    private MatchScheduleRepository matchScheduleRepository;

    @DisplayName("날짜를 입력하면 해당 날짜의 경기 일정을 찾을 수 있다.")
    @Test
    void findAllByMatchDate() {
        // given
        LocalDate matchDate = LocalDate.of(2025, 9, 28);

        MatchSchedule schedule1 = createSchedule(matchDate, "KT", "두산", "수원");
        MatchSchedule schedule2 = createSchedule(matchDate, "한화", "SSG", "대전");
        MatchSchedule schedule3 = createSchedule(matchDate, "삼성", "키움", "대구");

        matchScheduleRepository.saveAll(List.of(schedule1, schedule2, schedule3));

        // when
        List<MatchSchedule> matchSchedules = matchScheduleRepository.findAllByMatchDate(matchDate);

        // then
        assertThat(matchSchedules).hasSize(3)
                .extracting("homeTeam", "awayTeam", "stadium")
                .containsExactlyInAnyOrder(
                        tuple("KT", "두산", "수원"),
                        tuple("한화", "SSG", "대전"),
                        tuple("삼성", "키움", "대구")
                );

    }

    @DisplayName("해당 날짜에 경기가 없으면 빈 리스트를 반환한다.")
    @Test
    void findAllByMatchDateWithoutSchedule() {
        // given
        LocalDate matchDate = LocalDate.of(2025, 9, 28);

        // when
        List<MatchSchedule> matchSchedules = matchScheduleRepository.findAllByMatchDate(matchDate);

        // then
        assertThat(matchSchedules).isEmpty();

    }

    @DisplayName("경기 날짜와 Home 팀, Away 팀 이름으로 경기 일정을 찾는다.")
    @Test
    void findByMatchDateAndHomeTeamAndAwayTeam() {
        // given
        LocalDate matchDate = LocalDate.of(2025, 9, 28);

        String home = "한화";
        String away = "SSG";

        MatchSchedule schedule1 = createSchedule(matchDate, "KT", "두산", "수원");
        MatchSchedule schedule2 = createSchedule(matchDate, home, away, "대전");
        MatchSchedule schedule3 = createSchedule(matchDate, "삼성", "키움", "대구");

        matchScheduleRepository.saveAll(List.of(schedule1, schedule2, schedule3));

        // when
        Optional<MatchSchedule> schedule = matchScheduleRepository.findByMatchDateAndHomeTeamAndAwayTeam(
                matchDate, home, away);

        // then
        assertThat(schedule).contains(schedule2);

    }

    @DisplayName("경기 일정 조회시 홈팀이 맞지 않으면 Optional.empty() 값을 반환한다.")
    @Test
    void findByMatchDateAndHomeTeamAndAwayTeamWithInvalidHomeTeam() {
        // given
        LocalDate matchDate = LocalDate.of(2025, 9, 28);

        String home = "한화";
        String away = "SSG";
        String invalidHome = "롯데";

        MatchSchedule schedule1 = createSchedule(matchDate, "KT", "두산", "수원");
        MatchSchedule schedule2 = createSchedule(matchDate, home, away, "대전");
        MatchSchedule schedule3 = createSchedule(matchDate, "삼성", "키움", "대구");

        matchScheduleRepository.saveAll(List.of(schedule1, schedule2, schedule3));

        // when
        Optional<MatchSchedule> schedule = matchScheduleRepository.findByMatchDateAndHomeTeamAndAwayTeam(
                matchDate, invalidHome, away);

        // then
        assertThat(schedule).isEmpty();

    }

    @DisplayName("경기 일정 조회시 어웨이팀이 맞지 않으면 Optional.empty() 값을 반환한다.")
    @Test
    void findByMatchDateAndHomeTeamAndAwayTeamWithInvalidAwayTeam() {
        // given
        LocalDate matchDate = LocalDate.of(2025, 9, 28);

        String home = "한화";
        String away = "SSG";
        String invalidAway = "롯데";

        MatchSchedule schedule1 = createSchedule(matchDate, "KT", "두산", "수원");
        MatchSchedule schedule2 = createSchedule(matchDate, home, away, "대전");
        MatchSchedule schedule3 = createSchedule(matchDate, "삼성", "키움", "대구");

        matchScheduleRepository.saveAll(List.of(schedule1, schedule2, schedule3));

        // when
        Optional<MatchSchedule> schedule = matchScheduleRepository.findByMatchDateAndHomeTeamAndAwayTeam(
                matchDate, home, invalidAway);

        // then
        assertThat(schedule).isEmpty();

    }

    @DisplayName("경기 일정 조회시 날짜가 맞지 않으면 Optional.empty() 값을 반환한다.")
    @Test
    void findByMatchDateAndHomeTeamAndAwayTeamWithInvalidMatchDate() {
        // given
        LocalDate matchDate = LocalDate.of(2025, 9, 28);

        LocalDate invalidMatchDate = LocalDate.of(2025, 9, 29);

        String home = "한화";
        String away = "SSG";

        MatchSchedule schedule1 = createSchedule(matchDate, "KT", "두산", "수원");
        MatchSchedule schedule2 = createSchedule(matchDate, home, away, "대전");
        MatchSchedule schedule3 = createSchedule(matchDate, "삼성", "키움", "대구");

        matchScheduleRepository.saveAll(List.of(schedule1, schedule2, schedule3));

        // when
        Optional<MatchSchedule> schedule = matchScheduleRepository.findByMatchDateAndHomeTeamAndAwayTeam(
                invalidMatchDate, home, away);

        // then
        assertThat(schedule).isEmpty();

    }


    private MatchSchedule createSchedule(LocalDate matchDate, String homeTeam, String awayTeam, String stadium) {
        return MatchSchedule.builder()
                .matchDate(matchDate)
                .homeTeam(homeTeam)
                .awayTeam(awayTeam)
                .stadium(stadium)
                .build();
    }
}