package basecamp.zikgwan.matchschedule.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class KboRequestDto {
    private int year;
    private int month;
    private int day;
}
