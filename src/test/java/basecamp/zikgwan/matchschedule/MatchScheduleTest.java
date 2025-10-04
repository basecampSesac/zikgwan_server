package basecamp.zikgwan.matchschedule;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class MatchScheduleTest {

    @DisplayName("경기가 확정될 경우 경기장 정보를 업데이트 한다.")
    @Test
    void updateStadium() {
        // given
        MatchSchedule schedule = MatchSchedule.builder()
                .matchDate(LocalDate.of(2025, 9, 28))
                .homeTeam("두산")
                .awayTeam("잠실")
                .build();

        String stadium = "잠실";

        // when
        schedule.updateStadium(stadium);

        // then
        assertThat(schedule.getStadium()).isEqualTo(stadium);
    }
}