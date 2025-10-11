package basecamp.zikgwan.user.controller;

import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.user.dto.UserResponseDto;
import basecamp.zikgwan.user.service.SocialLoginService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import java.util.UUID;
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
                + "&scope=profile_nickname,account_email"
                + "&prompt=login";
        System.out.println("카카오 로그인요청 URL : " + returnUrl);

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

        System.out.println("카카오에서 로그인 후 보내주는 code : " + code);
        String accessToken = getKakaoAccessToken(code);
        Map<String, Object> userInfo = getKakaoUserInfo(accessToken);

        String email = (String) ((Map<String, Object>) userInfo.get("kakao_account")).get("email");
        String nickname = (String) ((Map<String, Object>) ((Map<String, Object>) userInfo.get("kakao_account")).get(
                "profile")).get("nickname");

        redirectAfterLogin("kakao", email, nickname, response);
    }

    /**
     * 카카오 로그인 accessToken
     *
     * @param code
     * @return
     */
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

    /**
     * 카카오 로그인 사용자 정보
     *
     * @param accessToken
     * @return
     */
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
     * 네이버 로그인 URL호출
     *
     * @return
     */
    @GetMapping("/naver/loginUrl")
    public ResponseEntity<ApiResponse<String>> getAuthNaver() {
        String state = UUID.randomUUID().toString(); // CSRF 방지용 state 값 생성
        String returnUrl = "https://nid.naver.com/oauth2.0/authorize?"
                + "response_type=code"
                + "&client_id=" + naverClientId
                + "&redirect_uri=" + naverRedirectUri
                + "&state=" + state;
        System.out.println("네이버 로그인 요청 URL : " + returnUrl);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(returnUrl));
    }

    /**
     * 네이버 로그인 이후 사용자정보
     *
     * @param code
     * @param state
     * @param response
     * @throws Exception
     */
    @GetMapping("/naver/oauth2")
    public void naverLogin(@RequestParam("code") String code,
                           @RequestParam("state") String state,
                           HttpServletResponse response) throws Exception {

        System.out.println("네이버에서 로그인 후 보내주는 code : " + code);
        System.out.println("네이버에서 로그인 후 보내주는 state : " + state);

        // AccessToken 발급
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", naverClientId);
        params.add("client_secret", naverClientSecret);
        params.add("code", code);
        params.add("state", state);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        Map tokenResponse = restTemplate.postForObject("https://nid.naver.com/oauth2.0/token", request, Map.class);
        String accessToken = (String) tokenResponse.get("access_token");

        //사용자 정보 조회
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.add("Authorization", "Bearer " + accessToken);
        HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://openapi.naver.com/v1/nid/me",
                HttpMethod.GET,
                userRequest,
                Map.class
        );

        Map<String, Object> responseBody = (Map<String, Object>) userResponse.getBody().get("response");
        String email = (String) responseBody.get("email");
        String nickname = (String) responseBody.get("nickname");

        System.out.println("네이버로그인 이메일 : " + email);
        System.out.println("네이버로그인 nickname : " + nickname);

        redirectAfterLogin("naver", email, nickname, response);
    }

    /**
     * 구글로그인 URL
     *
     * @return
     */
    @GetMapping("/google/loginUrl")
    public ResponseEntity<ApiResponse<String>> getAuthGoogle() {
        String returnUrl = "https://accounts.google.com/o/oauth2/v2/auth"
                + "?client_id=" + googleClientId
                + "&redirect_uri=" + googleRedirectUri
                + "&response_type=code"
                + "&scope=openid%20email%20profile"
                + "&access_type=offline"
                + "&prompt=consent"; // 매번 로그인 강제

        System.out.println("구글 로그인요청 URL : " + returnUrl);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(returnUrl));
    }

    /**
     * 구글로그인  사용자정보 받아오기
     *
     * @param code
     * @param response
     * @throws Exception
     */
    @GetMapping("/google/oauth2")
    public void googleLogin(@RequestParam("code") String code,
                            HttpServletResponse response) throws Exception {
        // 1. AccessToken 발급
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.add("grant_type", "authorization_code");
        params.add("client_id", googleClientId);
        params.add("client_secret", googleClientSecret);
        params.add("redirect_uri", googleRedirectUri);
        params.add("code", code);

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(params, headers);
        Map tokenResponse = restTemplate.postForObject("https://oauth2.googleapis.com/token", request, Map.class);
        String accessToken = (String) tokenResponse.get("access_token");

        // 2. 사용자 정보 조회
        HttpHeaders userHeaders = new HttpHeaders();
        userHeaders.add("Authorization", "Bearer " + accessToken);
        HttpEntity<?> userRequest = new HttpEntity<>(userHeaders);

        ResponseEntity<Map> userResponse = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                HttpMethod.GET,
                userRequest,
                Map.class
        );

        Map<String, Object> responseBody = userResponse.getBody();
        
        String email = (String) responseBody.get("email");
        String nickname = (String) responseBody.get("name");

        redirectAfterLogin("google", email, nickname, response);
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
