package basecamp.zikgwan.notification.service;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.notification.Notification;
import basecamp.zikgwan.notification.dto.NotificationResponseDto;
import basecamp.zikgwan.notification.repository.NotificationRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    // 사용자의 모든 알림 조회
    // TODO 시큐리티 설정 필요
    public List<NotificationResponseDto> getNotifications(Long userId) {

        User findUser = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

        // 삭제되지 않은 알림만 조회
        List<Notification> notifications = notificationRepository.findAllByReceiverAndSaveState(findUser, SaveState.Y);

        return notifications.stream()
                .map(n -> NotificationResponseDto.builder()
                        .id(n.getId())
                        .roomId(n.getRoomId())
                        .message(n.getMessage())
                        .readAt(n.getReadAt())
                        .build())
                .collect(Collectors.toList());
    }

    // 알림 읽음 처리
    @Transactional
    public String readNotification(Long userId, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("알림이 존재하지 않습니다."));

        if (notification.getReceiver().getUserId() != userId) {
            throw new IllegalArgumentException("해당 유저의 알림이 아닙니다.");
        }

        notification.updateReadAt(LocalDateTime.now());

        log.info("알림 읽은 처리 확인 : {}", notification.getReadAt());

        notificationRepository.save(notification);

        return notification.getMessage() + " 알림 읽음 처리 완료";
    }

    // 해당 알림 삭제
    @Transactional
    public String deleteNotification(Long userId, Long id) {
        Notification notification = notificationRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("알림이 존재하지 않습니다."));

        if (notification.getReceiver().getUserId() != userId) {
            throw new IllegalArgumentException("해당 유저의 알림이 아닙니다.");
        }

        notification.updateSaveState(SaveState.N);

        notificationRepository.save(notification);

        return notification.getMessage() + " 알림 삭제 완료";
    }
}
