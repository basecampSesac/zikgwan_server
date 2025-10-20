package basecamp.zikgwan.user.controller;


import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.config.security.CustomUserPrincipal;
import basecamp.zikgwan.config.security.TokenProvider;
import basecamp.zikgwan.image.enums.ImageType;
import basecamp.zikgwan.image.service.ImageService;
import basecamp.zikgwan.user.domain.Token;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.dto.UserRequestDto;
import basecamp.zikgwan.user.dto.UserResponseDto;
import basecamp.zikgwan.user.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/user")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;
    private final ImageService imageService;

    //jwt 적용
    @Autowired
    private TokenProvider tokenProvider;

    //패스워드 암호화
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 닉네임 중복확인 /api/user/chknickname
     *
     * @param body
     * @return
     */
    @PostMapping("/chknickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestBody Map<String, String> body) {
        String nickname = body.get("nickname");
        System.out.println("찾을 닉네임 : " + nickname);
        Boolean chkResult = userService.checkNickname(nickname);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(chkResult));
    }

    /**
     * 회원가입 /api/user/signup
     *
     * @param userDTO
     * @return
     */
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(@RequestBody @Valid UserRequestDto userDTO) {

        if (userDTO.getPassword() == null) {
            throw new IllegalArgumentException("비밀번호가 입력되지 않았습니다.");
        }
        User user = User.builder().email(userDTO.getEmail()).nickname(userDTO.getNickname())
                .password(passwordEncoder.encode(userDTO.getPassword())).club(userDTO.getClub()).saveState(SaveState.Y)
                .provider("local")
                .build();

        //System.out.println("************" +userDTO.getPassword());

        User registeredUser = userService.registerUser(user);
        UserResponseDto rsUserDTO = UserResponseDto.builder().email(registeredUser.getEmail())
                .nickname(registeredUser.getNickname()).userId(registeredUser.getUserId())
                .provider(registeredUser.getProvider())
                .club(registeredUser.getClub()).build();

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(rsUserDTO));

    }

    /**
     * 사용자 정보 수정 /api/user/{id}
     *
     * @param id
     * @param userDTO
     * @param principal
     * @return
     */
    @PutMapping("/{id:[0-9]+}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(@PathVariable Long id,
                                                                   @RequestBody @Valid UserRequestDto userDTO,
                                                                   @AuthenticationPrincipal CustomUserPrincipal principal) {

        if (principal == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(ApiResponse.fail("로그인정보가 없습니다."));
        }
        if (!id.equals(principal.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("권한이 없습니다."));
        }

        User user = User.builder().email(userDTO.getEmail()).nickname(userDTO.getNickname())
                .password(userDTO.getPassword()).club(userDTO.getClub()).saveState(SaveState.Y)
                .build();
        String newPassword = userDTO.getNewpassword();
        String newPasswordConfirm = userDTO.getNewpasswordconfirm();

        User updateUser = userService.updateUser(id, user, newPassword, newPasswordConfirm);

        UserResponseDto rsUserDTO = UserResponseDto.builder().email(updateUser.getEmail())
                .nickname(updateUser.getNickname()).userId(updateUser.getUserId()).club(updateUser.getClub())
                .provider(updateUser.getProvider()).build();

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(rsUserDTO));

    }

    /**
     * 로그인 /api/user/login
     *
     * @param userDTO
     * @return
     */
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponseDto>> userLogin(@RequestBody @Valid UserRequestDto userDTO,
                                                                  HttpServletResponse response) {
        User user = userService.userLogin(userDTO.getEmail(), userDTO.getPassword(), passwordEncoder);

        // 로그인 검사 통과!
        if (user != null) {

            //Date expiryDate = new Date(System.currentTimeMillis() + REFRESH_TOKEN_EXPIRE_TIME);
            Date expiryDate = Date.from(Instant.now().plus(7, ChronoUnit.DAYS));

            // jwt 적용
            // Access 토큰발급
            final String token = tokenProvider.create(user);
            // 리프레쉬 토큰
            final String refreshToken = tokenProvider.createRefreshToken(user, expiryDate);

            Token tokenEntity = Token.builder().expiryDate(LocalDateTime.now().plusDays(7)).refreshToken(refreshToken)
                    .user(user).build();

            userService.refreshTokenSave(tokenEntity);

            //Refresh Token을 HttpOnly 쿠키로 저장
            Cookie refreshCookie = new Cookie("refreshToken", refreshToken);
            refreshCookie.setHttpOnly(true); // JavaScript 접근 불가
            refreshCookie.setSecure(false);  // HTTPS 시 true
            refreshCookie.setPath("/");
            refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7일
            refreshCookie.setAttribute("SameSite", "None"); // 크로스 도메인 쿠키 허용
            response.addCookie(refreshCookie);

            // Access Token과 사용자 정보는 본문으로 반환
            final UserResponseDto responseUserDTO = UserResponseDto.builder()
                    .email(user.getEmail())
                    .userId(user.getUserId())
                    .nickname(user.getNickname())
                    .provider(user.getProvider())
                    .club(user.getClub())
                    .token(token) // 토큰
                    .build();

            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(responseUserDTO));
        } else {
            //로그인 검사 실패!(해당 유저가 존재하지 않았으므로)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("로그인 실패"));
        }
    }

    /**
     * 회원탈퇴 /api/user/delete/{id}
     *
     * @param id
     * @param principal
     * @return
     */
    @PatchMapping("/delete/{id:[0-9]+}")
    public ResponseEntity<ApiResponse<Boolean>> userDelete(@PathVariable Long id,
                                                           @AuthenticationPrincipal CustomUserPrincipal principal) {

        if (!id.equals(principal.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("권한이 없습니다."));
        }
        Boolean successData = userService.userDelete(id);

        if (successData) {
            return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(successData));
        } else {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("탈퇴가 정상적으로 처리되지 않았습니다."));
        }
    }

    /**
     * 로그아웃 /api/user/logout
     *
     * @param principal
     * @return
     */
    @GetMapping("/logout")
    public ResponseEntity<ApiResponse<Boolean>> logout(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                       HttpServletResponse response) {

        //로그인된 사용자 정보
        Long userId = principal.getUserId();

        if (userId < 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.fail("사용자 정보가 유효하지 않습니다."));
        }

        // DB에서 Refresh Token 삭제
        Boolean successData = userService.deleteRefreshTokenByUserId(userId);

        // 쿠키 삭제
        Cookie deleteCookie = new Cookie("refreshToken", null);
        deleteCookie.setPath("/");
        deleteCookie.setMaxAge(0);
        response.addCookie(deleteCookie);

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(successData));
    }

    /**
     * 사용자정보조회 /api/user/{id}
     *
     * @param id
     * @param principal
     * @return
     */
    @GetMapping("/{id:[0-9]+}")
    public ResponseEntity<ApiResponse<UserResponseDto>> getUserInfo(@PathVariable Long id,
                                                                    @AuthenticationPrincipal CustomUserPrincipal principal) {
        if (!id.equals(principal.getUserId())) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body(ApiResponse.fail("권한이 없습니다."));
        }

        User user = userService.getUserInfo(id);

        String imageUrl = null;
        imageUrl = imageService.getImage(ImageType.U, user.getUserId());
        //System.out.println("사용자 정보 imageUrl : " + imageUrl);

        UserResponseDto rsUserDTO = UserResponseDto.builder().email(user.getEmail()).nickname(user.getNickname())
                .userId(user.getUserId()).club(user.getClub()).imageUrl(imageUrl).provider(user.getProvider()).build();

        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success(rsUserDTO));
    }

    /**
     * 리프래쉬 토큰으로 액세스 토큰 생성
     *
     * @param request
     * @return
     */
    @PostMapping("/refresh/login")
    public ResponseEntity<ApiResponse<UserResponseDto>> refresh(HttpServletRequest request,
                                                                HttpServletResponse response) {

        // 쿠키에서 refreshToken 꺼내기
        String refreshToken = Arrays.stream(request.getCookies() != null ? request.getCookies() : new Cookie[]{})
                .filter(cookie -> "refreshToken".equals(cookie.getName()))
                .map(Cookie::getValue)
                .findFirst()
                .orElse(null);

        if (refreshToken == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("Refresh Token이 없습니다."));
        }

        // Refresh Token을 이용해 Access Token 재발급
        UserResponseDto responseDto = userService.refreshAccessToken(refreshToken);

        if (responseDto == null) {
            // 쿠키 무효화
            Cookie expiredCookie = new Cookie("refreshToken", null);
            expiredCookie.setPath("/");
            expiredCookie.setMaxAge(0);
            response.addCookie(expiredCookie);
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(ApiResponse.fail("유효하지 않은 리프레시 토큰입니다."));
        }

        return ResponseEntity.ok(ApiResponse.success(responseDto));
    }

    /**
     * 사용자 비밀번호 재설정
     *
     * @param userDTO
     * @return
     */
    @PostMapping("/pwReset")
    public ResponseEntity<ApiResponse<UserResponseDto>> passwordReset(@RequestBody @Valid UserRequestDto userDTO) {

        User updateUser = userService.passwordReset(userDTO);

        UserResponseDto rsUserDTO = UserResponseDto.builder().email(updateUser.getEmail())
                .nickname(updateUser.getNickname()).userId(updateUser.getUserId())
                .club(updateUser.getClub()).provider(updateUser.getProvider()).build();

        return ResponseEntity.ok(ApiResponse.success(rsUserDTO));

    }

}
