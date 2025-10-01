package basecamp.zikgwan.notification.controller;

import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.notification.dto.NotificationResponseDto;
import basecamp.zikgwan.notification.service.NotificationService;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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

    // TODO 시큐리티 적용 시 userId 빠짐

    /**
     * 해당 사용자의 모든 알림 목록 조회 읽지 않은 알림은 readAt = null
     */
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<NotificationResponseDto>>> getNotifications(@PathVariable Long userId) {

        List<NotificationResponseDto> notificationResponseDtos = notificationService.getNotifications(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(notificationResponseDtos));
    }

    /**
     * 알림 읽음 처리
     */
    @PatchMapping("/{userId}/{id}")
    public ResponseEntity<ApiResponse<String>> readNotification(@PathVariable Long userId, @PathVariable Long id) {

        String message = notificationService.readNotification(userId, id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(message));
    }

    /**
     * 알림 삭제
     */
    @DeleteMapping("/{userId}/{id}")
    public ResponseEntity<ApiResponse<String>> deleteNotification(@PathVariable Long userId, @PathVariable Long id){

        String message = notificationService.deleteNotification(userId, id);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(message));

    }


}
