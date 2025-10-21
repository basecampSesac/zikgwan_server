package basecamp.zikgwan.user.service;


import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.config.security.TokenProvider;
import basecamp.zikgwan.email.enums.VerifiedType;
import basecamp.zikgwan.email.service.EmailVerificationService;
import basecamp.zikgwan.user.domain.Token;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.dto.UserRequestDto;
import basecamp.zikgwan.user.dto.UserResponseDto;
import basecamp.zikgwan.user.repository.TokenRepository;
import basecamp.zikgwan.user.repository.UserRepository;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserService {

    private final UserRepository userRepository;
    private final TokenRepository tokenRepository;
    private final TokenProvider tokenProvider;

    private final EmailVerificationService emailVerifyService;

    //private final PasswordEncoder passwordEncoder;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    /**
     * 닉네임중복확인
     *
     * @param nickname
     * @return
     */
    public Boolean checkNickname(String nickname) {
        return userRepository.existsByNickname(nickname);
    }

    public Boolean checkEmail(String email) {
        return userRepository.existsByEmail(email);
    }

    /**
     * 회원가입
     *
     * @param user
     * @return
     */
    @Transactional
    public User registerUser(final User user) {

        //인증 유효범위 시간체크를 위한 현재시간
        LocalDateTime nowTime = LocalDateTime.now();

        //유효성 검사 이메일 인증결과 확인
        boolean result = emailVerifyService.isVerified(user.getEmail(), "S");
        if (!result) {
            throw new IllegalArgumentException("이메일 인증결과 확인이 실패하였습니다.");
        }
        //유효성 검사 userEntity 혹은 email 이 null 인 경우 예외 던짐
        if (user.getEmail() == null) {
            throw new RuntimeException("Invalid arguments");
        }

        //인증요청한 시간에서 10분이내로 회원가입가능
        LocalDateTime emailVerificationTime = emailVerifyService.verifiedTimeCheck(user.getEmail(), VerifiedType.S);
        emailVerificationTime = emailVerificationTime.plusMinutes(10);
        if (!emailVerificationTime.isAfter(nowTime)) {
            throw new IllegalArgumentException("회원가입 가능한 유효시간이 초과하였습니다.");
        }

        final String email = user.getEmail();

        //유효성 검사 이메일이 이미 존재하는 경우 예외를 던짐 (email 필드는 유니크해야 하므로)
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("이메일이 이미 존재합니다." + email);
        }

        // 유효성 검새 닉네임이 이미존재하는 경우 오류(중복불가)
        if (userRepository.existsByNickname(user.getNickname())) {
            throw new IllegalArgumentException("닉네임이 이미 존재합니다.");
        }

        return userRepository.save(user);

    }

    /**
     * 사용자정보 업데이트
     *
     * @param id
     * @param user
     * @param newPassword
     * @param newPasswordConfirm
     * @return
     */
    @Transactional
    public User updateUser(Long id, final User user, String newPassword, String newPasswordConfirm) {

        User chkUser = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        if ("local".equals(chkUser.getProvider())) {
            if (user.getPassword() == null) {
                throw new IllegalArgumentException("현재비밀번호 입력안됨");
            }

            if (newPassword != null && !newPassword.equals(newPasswordConfirm)) {
                throw new IllegalArgumentException("새로운비밀번호와 새로운비밀번호확인 불일치");
            }

            if (!passwordEncoder.matches(user.getPassword(), chkUser.getPassword())) {

                throw new IllegalArgumentException("현재비밀번호 불일치");
            }
        }

        String updateNickname = null;
        if (user.getNickname() == null) {
            updateNickname = chkUser.getNickname();
        } else {
            updateNickname = user.getNickname();
            //유효성 검사(닉네임 변경시 닉네임 유효성 검사
            if (userRepository.countByUserIdNotAndNickname(id, user.getNickname()) > 0) {
                throw new IllegalArgumentException("닉네임이 이미 존재 합니다.");
            }
        }

        String newEncodedPassword = null;
        if ("local".equals(chkUser.getProvider()) && newPassword != null && !newPassword.isEmpty()) {
            newEncodedPassword = passwordEncoder.encode(newPassword);
            //새 비밀번호가 기존 비밀번호와 동일한지 확인
            if (passwordEncoder.matches(newPassword, chkUser.getPassword())) {
                throw new IllegalArgumentException("새 비밀번호는 기존 비밀번호와 달라야 합니다.");
            }
        } else {
            newEncodedPassword = chkUser.getPassword();
        }

        User updateuser = User.builder().club(user.getClub()).nickname(updateNickname).password(newEncodedPassword)
                .saveState(SaveState.Y).email(chkUser.getEmail()).userId(id).provider(chkUser.getProvider()).build();

        return userRepository.save(updateuser);

    }

    /**
     * 사용자로그인
     *
     * @param email
     * @param password
     * @param encoder
     * @return
     */
    public User userLogin(final String email, final String password, final PasswordEncoder encoder) {
        final User originalUser = userRepository.findByEmail(email);

        if (originalUser == null) {
            throw new IllegalArgumentException("존재하지 않는 이메일입니다.");
        }

        if (!originalUser.getProvider().equals("local")) {
            throw new IllegalArgumentException("소셜로그인 사용자입니다." + originalUser.getProvider() + "로그인을 이용해주세요.");
        }

        //탈퇴한 회원이면 로그인 불가
        if (originalUser.getSaveState().equals(SaveState.N)) {
            return null;
        }

        if (originalUser != null && encoder.matches(password, originalUser.getPassword())) {
            // password :  클라이언트가 주장하는 현재 유저에 대한 비밀번호
            // originalUser.getPassword() : DB 에 저장된 정답 비밀번호
            return originalUser;
        }

        return null;
    }

    /**
     * 리프래쉬 토큰 저장
     *
     * @param token
     */
    @Transactional
    public void refreshTokenSave(Token token) {
        System.out.println("refreshTokenSave deleteByUserId " + token.getUser().toString());
        tokenRepository.deleteByUserId(token.getUser().getUserId());
        tokenRepository.save(token);
    }

    /**
     * 사용자 삭제 //save_status=N 으로 업데이트
     *
     * @param id
     * @return
     */
    @Transactional
    public Boolean userDelete(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new RuntimeException("User not found"));

        User userSaveState = userRepository.findByUserId(id);

        if (userSaveState.getSaveState().equals(SaveState.N)) {
            return false;
        }

        //회원탈퇴시 리프래쉬 토큰 삭제
        Boolean deleteRefreshToken = deleteRefreshTokenByUserId(id);
        System.out.println("회원탈퇴시 리프래쉬 토큰 삭제" + deleteRefreshToken);

        Boolean deleteResult = false;
        if (userRepository.updateSaveStatusToN(id) > 0) {
            deleteResult = true;
        } // 저장상태 n 호출 (변경 감지 후 반영)
        return deleteResult;
    }

    /**
     * 리프레쉬토큰 삭제
     *
     * @param userId
     * @return
     */
    @Transactional
    public boolean deleteRefreshTokenByUserId(Long userId) {
        //리프래시 토큰 찾아서 토큰이 있으면 삭제
        tokenRepository.findByUserUserId(userId).ifPresentOrElse(
                token -> {
                    tokenRepository.delete(token);
                    System.out.println("Refresh Token 삭제  userId : " + userId);
                },
                () -> System.out.println("Refresh Token 없음 userId : " + userId)
        );
        return true;
    }

    //사용자정보조회
    public User getUserInfo(Long id) {
        User userInfo = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("존재하지 않는 회원"));

        return userInfo;
    }

    /**
     * 리프래쉬토큰으로 액세스 토큰 발행
     *
     * @param refreshToken
     * @return
     */
    public UserResponseDto refreshAccessToken(String refreshToken) {

        Token tokenEntity = tokenRepository.findByRefreshToken(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (tokenEntity.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired");
        }

        String newAccessToken = tokenProvider.create(tokenEntity.getUser());

        return UserResponseDto.builder()
                .userId(tokenEntity.getUser().getUserId())
                .email(tokenEntity.getUser().getEmail())
                .nickname(tokenEntity.getUser().getNickname())
                .provider(tokenEntity.getUser().getProvider())
                .club(tokenEntity.getUser().getClub())
                .token(newAccessToken)
                .refreshToken(null)//null or refreshToken
                .build();

    }

    /**
     * 비밀번호 재설정
     *
     * @param userDto
     * @return
     */
    @Transactional
    public User passwordReset(final UserRequestDto userDto) {

        //인증 유효범위 시간체크를 위한 현재시간
        LocalDateTime nowTime = LocalDateTime.now();

        //유효성 검사 이메일 인증결과 확인
        boolean result = emailVerifyService.isVerified(userDto.getEmail(), "P");
        if (!result) {
            throw new IllegalArgumentException("이메일 인증결과 확인이 실패하였습니다.");
        }

        //인증요청한 시간에서 10분이내로 회원가입가능
        LocalDateTime emailVerificationTime = emailVerifyService.verifiedTimeCheck(userDto.getEmail(), VerifiedType.P);
        emailVerificationTime = emailVerificationTime.plusMinutes(10);
        if (!emailVerificationTime.isAfter(nowTime)) {
            throw new IllegalArgumentException("비밀번호 재설정 가능한 유효시간이 초과하였습니다.");
        }

        //유효성 검사 userEntity 혹은 email 이 null 인 경우 예외 던짐
        if (userDto.getEmail() == null) {
            throw new RuntimeException("Invalid arguments");
        }
        String newPassword = userDto.getNewpassword();
        String newPasswordConfirm = userDto.getNewpasswordconfirm();

        if (newPassword != null && !newPassword.equals(newPasswordConfirm)) {
            throw new IllegalArgumentException("새로운비밀번호와 새로운비밀번호 확인 불일치");
        }

        User user = userRepository.findByEmail(userDto.getEmail());

        //소셜로그인 사용자는 비밀번호 재설정 불가
        if (!user.getProvider().equals("local")) {
            throw new IllegalArgumentException("소셜로그인 사용자는 비밀번호 재설정 불가");
        }
        String newEncodedPassword = passwordEncoder.encode(newPassword);
        System.out.println("newEncodedPassword : " + newEncodedPassword);

        User updateuser = User.builder()
                .club(user.getClub())
                .nickname(user.getNickname())
                .password(newEncodedPassword)
                .saveState(SaveState.Y)
                .email(user.getEmail())
                .userId(user.getUserId())
                .provider(user.getProvider()).build();

        return userRepository.save(updateuser);

    }

}

