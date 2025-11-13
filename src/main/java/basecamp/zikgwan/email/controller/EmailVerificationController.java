package basecamp.zikgwan.email.controller;


import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.email.dto.EmailVerificationDTO;
import basecamp.zikgwan.email.enums.VerifiedType;
import basecamp.zikgwan.email.service.EmailVerificationService;
import basecamp.zikgwan.user.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@RestControllerAdvice
public class EmailVerificationController {

    private final EmailVerificationService emailVerifyService;
    private final UserService userService;

    /**
     * 이메일 인증코드 요청
     *
     * @param emailVerificationDTO
     * @return
     */
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendCode(@RequestBody @Valid EmailVerificationDTO emailVerificationDTO) {

        if (userService.checkEmail(emailVerificationDTO.getEmail())
                && emailVerificationDTO.getVerifiedType().equals(VerifiedType.S)) {
            System.out.println("이메일로 이미 가입한 회원이 존재합니다.");
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.fail("EMAILEXIST"));
        } else {
            emailVerifyService.sendVerificationCode(emailVerificationDTO.getEmail(),
                    emailVerificationDTO.getVerifiedType());

            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success("인증 코드가 발송되었습니다."));
        }
    }

    /**
     * 인증 코드 검증
     *
     * @param emailVerificationDTO
     * @return
     */
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyCode(
            @RequestBody @Valid EmailVerificationDTO emailVerificationDTO) {
        boolean result = emailVerifyService.verifyCode(emailVerificationDTO.getEmail(), emailVerificationDTO.getCode(),
                emailVerificationDTO.getVerifiedType());

        System.out.println("email : " + emailVerificationDTO.getEmail());
        System.out.println("code : " + emailVerificationDTO.getCode());

        if (result) {
            return ResponseEntity
                    .status(HttpStatus.OK)
                    .body(ApiResponse.success("이메일 인증이 완료되었습니다."));
        } else {
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body(ApiResponse.fail("이메일 인증 실패"));
        }
    }
}
