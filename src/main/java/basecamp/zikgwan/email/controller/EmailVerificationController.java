package basecamp.zikgwan.email.controller;


import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.email.dto.EmailVerificationDTO;
import basecamp.zikgwan.email.service.EmailVerificationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestController
@RequestMapping("/api/email")
@RequiredArgsConstructor
@RestControllerAdvice
public class EmailVerificationController {

    private final EmailVerificationService emailVerifyService;

    // 인증 코드 발송
    @PostMapping("/send")
    public ResponseEntity<ApiResponse<String>> sendCode(@RequestBody @Valid  EmailVerificationDTO emailVerificationDTO) {
        emailVerifyService.sendVerificationCode(emailVerificationDTO.getEmail());
        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success("인증 코드가 발송되었습니다."));
    }

    // 인증 코드 검증
    @PostMapping("/verify")
    public ResponseEntity<ApiResponse<String>> verifyCode(@RequestBody @Valid EmailVerificationDTO emailVerificationDTO) {
        boolean result = emailVerifyService.verifyCode(emailVerificationDTO.getEmail(), emailVerificationDTO.getCode());

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
