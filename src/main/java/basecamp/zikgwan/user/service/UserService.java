package basecamp.zikgwan.user.service;


import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.email.service.EmailVerificationService;
import basecamp.zikgwan.user.User;
import basecamp.zikgwan.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    private final EmailVerificationService emailVerifyService;

    //private final PasswordEncoder passwordEncoder;
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    //닉네임 중복확인
    public Boolean checkNickname(String nickname){
        return userRepository.existsByNickname(nickname);
    }
    //회원가입
    public User registerUser(final User user){

        //유효성 검사 이메일 인증결과 확인
        boolean result = emailVerifyService.isVerified(user.getEmail());
        if (!result) {
            throw  new RuntimeException("Email verified fail");
        }
        //유효성 검사 userEntity 혹은 email 이 null 인 경우 예외 던짐
        if(user.getEmail() == null){
            throw new RuntimeException("Invalid arguments");
        }

        final String email = user.getEmail();

        //유효성 검사 이메일이 이미 존재하는 경우 예외를 던짐 (email 필드는 유니크해야 하므로)
        if(userRepository.existsByEmail(email)){
            throw new RuntimeException("Email already exists" + email);
        }

        // 유효성 검새 닉네임이 이미존재하는 경우 오류(중복불가)
        if(userRepository.existsByNickname(user.getNickname())){
            throw new RuntimeException("Nicknmae already exists");
        }

        return userRepository.save(user);

    }

    //회원정보 수정
    public User updateUser(Long id, final User user, String newPassword, String newPasswordConfirm){

        User chkUser=userRepository.findById(id)
                .orElseThrow(()-> new IllegalArgumentException("존재하지 않는 회원"));

        String updateNickname= null;
        if(user.getNickname() == null){
            updateNickname=chkUser.getNickname();
        }
        else{
            updateNickname=user.getNickname();
        }
        if(user.getPassword() == null){
            throw new IllegalArgumentException("현재비밀번호 입력안됨");
        }
        System.out.println("pass : "+user.getPassword());
        System.out.println("newpass : "+newPassword);
        System.out.println("newpassch : "+newPasswordConfirm);

        if(!newPassword.equals(newPasswordConfirm)) {
            throw new IllegalArgumentException("새로운비밀번호와 새로운비밀번호 불일치");
        }

        if(!passwordEncoder.matches(user.getPassword(), chkUser.getPassword())){
            throw new IllegalArgumentException("현재비밀번호 불일치");
        }

        String newEncodedPassword = null;
        newEncodedPassword = passwordEncoder.encode(newPassword);

        User updateuser = User.builder()
                .club(user.getClub())
                .nickname(updateNickname)
                .password(newEncodedPassword)
                .saveState(SaveState.Y)
                .email(chkUser.getEmail())
                .userId(id)
                .build();

        return  userRepository.save(updateuser);

    }

    //사용자 로그인
    public User userLogin(final String email,
                          final  String password,
                          final PasswordEncoder encoder){
        final User originalUser = userRepository.findByEmail(email);

        if(originalUser != null && encoder.matches(password, originalUser.getPassword())){
            // password :  클라이언트가 주장하는 현재 유저에 대한 비밀번호
            // originalUser.getPassword() : DB 에 저장된 정답 비밀번호
            return originalUser;
        }

        return null;
    }

    //사용자 삭제 //save_status=N 으로 업데이트
    public Boolean userDelete(Long id){
        User user = userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Boolean deleteResult = false;
        if(userRepository.updateSaveStatusToN(id)>0){
            deleteResult=true;
        } // 저장상태 n 호출 (변경 감지 후 반영)
        return deleteResult;
    }

}
