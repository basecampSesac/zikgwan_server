package basecamp.zikgwan.email.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class EmailVerificationDTO {
    private String email;
    private String code;
}
