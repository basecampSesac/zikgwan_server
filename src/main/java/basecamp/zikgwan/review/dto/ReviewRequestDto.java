package basecamp.zikgwan.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ReviewRequestDto {

    @Max(value = 5, message = "별점은 최대 5.0점까지 줄 수 있습니다.")
    @Min(value = 1, message = "별점은 최소 1.0점까지 줄 수 있습니다.")
    @NotNull(message = "별점이 비어있으면 안 됩니다.")
    private Double rating;

}
