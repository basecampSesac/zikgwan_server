package basecamp.zikgwan.matchschedule.dto;

import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
@AllArgsConstructor
public class KboResponseDto {
    private LocalDate date;    // 경기날짜
    private String home;    // 홈팀 코드
    private String away;    // 어웨이팀 코드
    private String place;   // 경기장 정보
}
