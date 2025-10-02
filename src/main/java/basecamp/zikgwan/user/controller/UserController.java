package basecamp.zikgwan.user.controller;


import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.config.security.TokenProvider;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.dto.UserRequestDto;
import basecamp.zikgwan.user.dto.UserResponseDto;
import basecamp.zikgwan.user.service.UserService;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
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

    //jwt 적용
    @Autowired
    private TokenProvider tokenProvider;

    //패스워드 암호화
    private PasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @PostMapping("/chknickname")
    public ResponseEntity<ApiResponse<Boolean>> checkNickname(@RequestBody Map<String, String> body){
        String nickname= body.get("nickname");
        System.out.println("찾을 닉네임 : "+ nickname);
        Boolean chkResult = userService.checkNickname(nickname);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(chkResult));
    }

    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<UserResponseDto>> registerUser(@RequestBody @Valid UserRequestDto userDTO){


        if (userDTO.getPassword() == null) {
            throw new IllegalArgumentException("비밀번호가 입력되지 않았습니다.");
        }
        User user = User.builder()
                .email(userDTO.getEmail())
                .nickname(userDTO.getNickname())
                .password(passwordEncoder.encode(userDTO.getPassword()))
                .club(userDTO.getClub())
                .saveState(SaveState.Y)
                .build();

        //System.out.println("************" +userDTO.getPassword());

        User registeredUser = userService.registerUser(user);
        UserResponseDto rsUserDTO = UserResponseDto.builder()
                .email(registeredUser.getEmail())
                .nickname(registeredUser.getNickname())
                .userId(registeredUser.getUserId())
                .club(registeredUser.getClub())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(rsUserDTO));

    }

    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<UserResponseDto>> updateUser(@PathVariable Long id, @RequestBody @Valid UserRequestDto userDTO){

        User user = User.builder()
                .email(userDTO.getEmail())
                .nickname(userDTO.getNickname())
                .password(userDTO.getPassword())
                .club(userDTO.getClub())
                .saveState(SaveState.Y)
                .build();
        String newPassword = userDTO.getNewpassword();
        String newPasswordConfirm = userDTO.getNewpasswordconfirm();

        User updateUser = userService.updateUser(id, user, newPassword, newPasswordConfirm);

        UserResponseDto rsUserDTO = UserResponseDto.builder()
                .email(updateUser.getEmail())
                .nickname(updateUser.getNickname())
                .userId(updateUser.getUserId())
                .club(updateUser.getClub())
                .build();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(rsUserDTO));

    }

    @PostMapping("/login")
    public ResponseEntity<ApiResponse<UserResponseDto>> userLogin(@RequestBody @Valid UserRequestDto userDTO){
        User user =  userService.userLogin(
                userDTO.getEmail(), userDTO.getPassword(), passwordEncoder
        );

        if(user != null){
            // 로그인 검사 통과!

            // jwt 적용
            final String token = tokenProvider.create(user);
            final UserResponseDto responseUserDTO = UserResponseDto.builder()
                    .email(user.getEmail())
                    .userId(user.getUserId())
                    .nickname(user.getNickname())
                    .token(token) // 토큰 설정
                    .build();

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success(responseUserDTO));
        }else {
            //로그인 검사 실패!(해당 유저가 존재하지 않았으므로)
            //UserResponseDto responseDTO = UserResponseDto.builder()
            //        .error("Login failed.")
            //        .build();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail("로그인 실패"));
        }
    }

    @PatchMapping("/delete/{id}")
    public ResponseEntity<ApiResponse<Boolean>> userDelete(@PathVariable Long id){

        Boolean  successData = userService.userDelete(id); // 예시: 서비스에서 삭제 후 DTO 반환

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(successData));
    }

}
