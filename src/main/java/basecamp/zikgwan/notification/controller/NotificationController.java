package basecamp.zikgwan.notification.controller;

import basecamp.zikgwan.common.aop.LoginCheck;
import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.config.security.CustomUserPrincipal;
import basecamp.zikgwan.notification.dto.EventPayload;
import basecamp.zikgwan.notification.dto.NotificationResponseDto;
import basecamp.zikgwan.notification.service.NotificationService;
import basecamp.zikgwan.notification.service.SseService;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;
    private final SseService sseService;

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

    /**
     * 알림 부하 테스트 용
     */
    @PostMapping("/broadcast/{userId}")
    public String sendNotification(@PathVariable Long userId,
                                   @RequestParam(defaultValue = "테스트 메시지") String message) {

        log.warn("[nGrinder 부하테스트] userId={}, message={}", userId, message);

        EventPayload payload = EventPayload.builder()
                .roomId(999L)
                .nickname("LoadTester")
                .message(message)
                .sentAt(LocalDateTime.now())
                .build();

        sseService.broadcast(userId, payload);

        return "userId=" + userId + "번 사용자에게 전송";
    }

}
