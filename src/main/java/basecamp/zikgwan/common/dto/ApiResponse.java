package basecamp.zikgwan.common.dto;

import lombok.Getter;

/**
 * 공통 API 응답 형식 지정
 */
@Getter
public class ApiResponse<T> {
    private final String status;    // success or error
    private final String message;   // 예외 메시지, 성공 시 null
    private final T data;           // 응답 데이터, 실패 시 null
    private final String redirectUrl; // 선택적 리다이렉트 경로

    public ApiResponse(String status, String message, T data, String redirectUrl) {
        this.status = status;
        this.message = message;
        this.data = data;
        this.redirectUrl=redirectUrl;
    }

    // 성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", null, data,null);
    }

    // 실패 응답
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>("error", message, null, null);
    }

    // 리다이렉트 포함 실패
    public static <T> ApiResponse<T> fail(String message, String redirectUrl) {
        return new ApiResponse<>("error", message, null, redirectUrl);
    }
}