package basecamp.zikgwan.user.controller;

import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.user.dto.UserResponseDto;
import basecamp.zikgwan.user.service.SocialLoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.client.RestTemplate;

@Controller
@RequiredArgsConstructor
@RequestMapping("/api/socialLogin")
public class SocialLoginController {
    private final SocialLoginService socialLoginService;
    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${kakao.client.id}")
    private String kakaoClientId;
    @Value("${kakao.client.secret}")
    private String kakaoClientSecret;
    @Value("${kakao.redirect.uri}")
    private String kakaoRedirectUri;

    @Value("${naver.client.id}")
    private String naverClientId;
    @Value("${naver.client.secret}")
    private String naverClientSecret;
    @Value("${naver.redirect.uri}")
    private String naverRedirectUri;

    @Value("${google.client.id}")
    private String googleClientId;
    @Value("${google.client.secret}")
    private String googleClientSecret;
    @Value("${google.redirect.uri}")
    private String googleRedirectUri;

    private final String FRONT_REDIRECT_URL = "http://localhost:5173/login";

    /**
     * 카카오 로그인 URL호출
     *
     * @return
     */
    @GetMapping("/kakao/loginUrl")
    public ResponseEntity<ApiResponse<String>> getAuthKakao() {
        String returnUrl = "https://kauth.kakao.com/oauth/authorize?"
                + "client_id=" + kakaoClientId
                + "&redirect_uri=" + kakaoRedirectUri
                + "&response_type=code"
                + "&scope=profile_nickname,account_email";
        System.out.println(returnUrl);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(returnUrl));
    }

    /**
     * 카카오 로그인 리턴
     *
     * @param code
     * @param response
     * @throws Exception
     */
    @GetMapping("/kakao/oauth2")
    public void kakaoLogin(@RequestParam("code") String code, HttpServletResponse response) throws Exception {
        String accessToken = getKakaoAccessToken(code);
        Map<String, Object> userInfo = getKakaoUserInfo(accessToken);

        String email = (String) ((Map<String, Object>) userInfo.get("kakao_account")).get("email");
        String nickname = (String) ((Map<String, Object>) ((Map<String, Object>) userInfo.get("kakao_account")).get(
                "profile")).get("nickname");

        redirectAfterLogin("kakao", email, nickname, response);
    }

    private String getKakaoAccessToken(String code) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", kakaoClientId);
        params.add("client_secret", kakaoClientSecret);
        params.add("redirect_uri", kakaoRedirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        Map tokenResponse = restTemplate.postForObject("https://kauth.kakao.com/oauth/token", request, Map.class);
        return (String) tokenResponse.get("access_token");
    }

    private Map<String, Object> getKakaoUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", "Bearer " + accessToken);
        HttpEntity<?> request = new HttpEntity<>(headers);

        ResponseEntity<Map> response = restTemplate.exchange(
                "https://kapi.kakao.com/v2/user/me",
                HttpMethod.GET,
                request,
                Map.class
        );
        return response.getBody();
    }


    /**
     * 공통 리다이렉트 처리
     *
     * @param provider
     * @param email
     * @param nickname
     * @param response
     * @throws Exception
     */
    private void redirectAfterLogin(String provider, String email, String nickname, HttpServletResponse response)
            throws Exception {
        UserResponseDto userDto = socialLoginService.handleLogin(provider, email, nickname);

        // refreshToken 쿠키 저장 (HttpOnly)
        Cookie refreshToken = new Cookie("refreshToken", userDto.getRefreshToken());
        refreshToken.setHttpOnly(true);
        refreshToken.setSecure(true);
        refreshToken.setPath("/");
        response.addCookie(refreshToken);

        // 프론트 리다이렉트 (쿼리스트링 닉네임 이메일 )
        String redirectUrl = FRONT_REDIRECT_URL
                + "?nickname=" + nickname
                + "&email=" + email;
        response.sendRedirect(redirectUrl);
    }
}
