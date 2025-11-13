package basecamp.zikgwan.matchschedule.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class KboRequestDto {

    @Positive(message = "년도는 양수여야 합니다.")
    @NotNull(message = "년도를 입력해주세요.")
    private int year;

    @Positive(message = "월은 양수여야 합니다.")
    @Min(value = 1, message = "1월 미만은 입력할 수 없습니다.")
    @Max(value = 12, message = "12월 초과는 입력할 수 없습니다.")
    @NotNull(message = "월을 입력해주세요.")
    private int month;

    @Positive(message = "일자는 양수여야 합니다.")
    @Min(value = 1, message = "1일 미만은 입력할 수 없습니다.")
    @Max(value = 31, message = "31일 초과는 입력할 수 없습니다.")
    @NotNull(message = "일자를 입력해주세요.")
    private int day;
}
