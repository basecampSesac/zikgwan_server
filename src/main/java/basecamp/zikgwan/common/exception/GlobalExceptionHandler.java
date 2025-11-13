package basecamp.zikgwan.common.exception;

import basecamp.zikgwan.common.dto.ApiResponse;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.beans.factory.annotation.Value;
/**
 * 글로벌 예외 처리
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    // application.properties에서 social login URL 가져오기
    @Value("${social.login.url}")
    private String socialLoginUrl;

    // 잘못된 요청 파라미터나 비즈니스 로직 예외
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ApiResponse<Void>> handleIllegalArgument(IllegalArgumentException e) {
        log.warn("잘못된 요청: {}", e.getMessage(), e);

        //소셜로그인 에러페이지 리다이렉트
        String msg = e.getMessage();
        if (msg != null && msg.startsWith("SOCIAL_LOGIN_ERROR")) {
            String cleanMessage = msg.replaceFirst("SOCIAL_LOGIN_ERROR \\s*", "");
            String redirectUrl = socialLoginUrl+"?error="
                    + URLEncoder.encode(cleanMessage, StandardCharsets.UTF_8);
            HttpHeaders headers = new HttpHeaders();
            headers.add("Location", redirectUrl);
            return new ResponseEntity<>(headers, HttpStatus.FOUND);
        }

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(e.getMessage()));
    }

    // 리소스를 찾지 못했을 때
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<ApiResponse<Void>> handleNoSuchElement(NoSuchElementException e) {
        log.warn("리소스 없음: {}", e.getMessage(), e);
        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(ApiResponse.fail(e.getMessage()));
    }

    //유효성 검사에대한 오류
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<String>> handleValidationExceptions(MethodArgumentNotValidException e) {
        log.warn("정규식 오류: {}", e.getMessage(), e);
        String errors = e.getBindingResult()
                .getAllErrors()
                .stream()
                .map(error -> {
                    String fieldName = ((FieldError) error).getField();
                    String message = error.getDefaultMessage();
                    return fieldName + ": " + message;
                })
                .collect(Collectors.joining(", "));

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(ApiResponse.fail(errors));
    }

    // 로그인 필요 예외
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ApiResponse<Void>> handleUnauthorized(UnauthorizedException e) {
        return ResponseEntity
                .status(e.getStatus())
                .body(ApiResponse.fail(e.getMessage()));
    }

    // 예상치 못한 서버 오류
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponse<Void>> handleException(Exception e) {
        log.error("서버 오류 발생", e);
        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(ApiResponse.fail("서버 내부 오류가 발생했습니다. 잠시 후 다시 시도해주세요."));
    }
}