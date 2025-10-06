package basecamp.zikgwan.community.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CommunityRequest {

    @NotBlank(message = "모임 제목을 입력해주세요.")
    @Size(max = 100, message = "모임 제목은 100자 이내로 입력해주세요.")
    private String title;

    @Size(max = 1000, message = "모임 설명은 1000자 이내로 입력해주세요.")
    private String description;

    @NotNull(message = "경기 일정을 선택해주세요.")
    private Long scheduleId;

    @NotNull(message = "모임 인원을 입력해주세요.")
    @Positive(message = "모임 인원은 1명 이상이어야 합니다.")
    private Integer memberCount;
}
