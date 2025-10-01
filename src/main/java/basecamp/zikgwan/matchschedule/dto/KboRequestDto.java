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

    @Positive
    @NotNull
    private int year;

    @Positive
    @Min(1)
    @Max(12)
    @NotNull
    private int month;

    @Positive
    @Min(1)
    @Max(31)
    @NotNull
    private int day;
}
