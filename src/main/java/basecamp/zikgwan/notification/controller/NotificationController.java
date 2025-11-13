package basecamp.zikgwan.notification.controller;

import basecamp.zikgwan.common.aop.LoginCheck;
import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.config.security.CustomUserPrincipal;
import basecamp.zikgwan.notification.dto.NotificationResponseDto;
import basecamp.zikgwan.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    /**
     * 해당 사용자의 모든 알림 목록 조회 읽지 않은 알림은 readAt = null
     */
    @LoginCheck
    @GetMapping("/all")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getNotifications(
            @AuthenticationPrincipal CustomUserPrincipal principal) {

        List<NotificationResponseDto> notificationResponseDtos = notificationService.getNotifications(
                principal.getUserId());

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(notificationResponseDtos));
    }

    /**
     * 알림 읽음 처리
     */
    @LoginCheck
    @PatchMapping("/read/{id}")
    public ResponseEntity<ApiResponse<String>> readNotification(@AuthenticationPrincipal CustomUserPrincipal principal,
                                                                @PathVariable Long id) {

        String message = notificationService.readNotification(principal.getUserId(), id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(message));
    }

    /**
     * 알림 삭제
     */
    @LoginCheck
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(
            @AuthenticationPrincipal CustomUserPrincipal principal, @PathVariable Long id) {

        String message = notificationService.deleteNotification(principal.getUserId(), id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(message));

    }
}
