package basecamp.zikgwan.user.service;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.config.security.TokenProvider;
import basecamp.zikgwan.user.domain.Token;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.dto.UserResponseDto;
import basecamp.zikgwan.user.repository.UserRepository;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import java.util.Random;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class SocialLoginService {
    private final UserRepository userRepository;
    private final TokenProvider tokenProvider;
    private final UserService userService;

    // 공통 로그인 처리
    public UserResponseDto handleLogin(String provider, String email, String nickname) {

        //이메일 계정으로 회원존재여부 확인
        User user = userRepository.findByEmail(email);

        String existingProvider = null;

        if (user == null) {

            //닉네임이 존재하는경우 닉네임정보 임의로 수정
            if (userService.checkNickname(nickname)) {

                String code = String.format("%06d", new Random().nextInt(99999999));
                nickname = nickname.trim() + code;
            }
            //신규회원 가입
            user = User.builder().email(email).nickname(nickname).provider(provider).saveState(SaveState.Y).build();
            user = userRepository.save(user);
        } else {
            existingProvider = user.getProvider();
            // 이미 존재
            if (existingProvider.equals("local")) {
                //일반 이메일 회원이 소셜 로그인 시도한 경우
                throw new IllegalArgumentException("SOCIAL_LOGIN_ERROR 해당 이메일은 일반 회원가입으로 이미 존재합니다. 이메일 로그인으로 시도해주세요.");
            }

            if (!existingProvider.equals(provider)) {
                // 다른 소셜 계정으로 가입된 경우
                throw new IllegalArgumentException(
                        "SOCIAL_LOGIN_ERROR 해당 이메일은 " + existingProvider + " 계정으로 이미 가입되어 있습니다.");
            }
            if (user.getSaveState().equals(SaveState.N)) {
                throw new IllegalArgumentException("SOCIAL_LOGIN_ERROR 해당 이메일은 탈퇴된 메일계정입니다.");
            }

        }

        System.out.println("provider : " + provider + ",existingProvider : " + existingProvider);

        // Access Token, Refresh Token 발급
        String accessToken = tokenProvider.create(user);

        Date expiryDate = Date.from(Instant.now().plus(7, ChronoUnit.DAYS));
        String refreshToken = tokenProvider.createRefreshToken(user, expiryDate);

        Token tokenEntity = Token.builder().expiryDate(LocalDateTime.now().plusDays(7)).refreshToken(refreshToken)
                .accessToken(accessToken).user(user).build();

        userService.refreshTokenSave(tokenEntity);

        final UserResponseDto responseUserDTO = UserResponseDto.builder().email(user.getEmail())
                .userId(user.getUserId()).nickname(user.getNickname()).token(accessToken) // 토큰 설정
                .refreshToken(refreshToken) // 리프레쉬토큰
                .club(user.getClub()).build();

        return responseUserDTO;
    }
}
