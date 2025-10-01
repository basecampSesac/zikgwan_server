package basecamp.zikgwan.user.dto;


import basecamp.zikgwan.common.enums.SaveState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserRequestDto {
    private String email;
    private String nickname;
    private String password;
    private String club;
    private SaveState saveState;
    private String newpassword;
    private String newpasswordconfirm;


}
