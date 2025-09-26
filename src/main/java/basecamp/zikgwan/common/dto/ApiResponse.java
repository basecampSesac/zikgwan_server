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

    public ApiResponse(String status, String message, T data) {
        this.status = status;
        this.message = message;
        this.data = data;
    }

    // 성공 응답
    public static <T> ApiResponse<T> success(T data) {
        return new ApiResponse<>("success", null, data);
    }

    // 실패 응답
    public static <T> ApiResponse<T> fail(String message) {
        return new ApiResponse<>("error", message, null);
    }
}