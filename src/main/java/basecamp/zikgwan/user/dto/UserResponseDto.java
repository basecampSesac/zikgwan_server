package basecamp.zikgwan.user.dto;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Data
@AllArgsConstructor
@Builder
public class UserResponseDto {
    private String nickname;
    private String email;
    private String club;
    private Long userId;
    private String token;
    private String refreshToken;
    private String provider;

    private String imageUrl; //프로필 이미지 URL


}
