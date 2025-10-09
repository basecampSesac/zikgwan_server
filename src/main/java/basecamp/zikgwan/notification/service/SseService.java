package basecamp.zikgwan.notification.service;


import basecamp.zikgwan.notification.Notification;
import basecamp.zikgwan.notification.dto.EventPayload;
import basecamp.zikgwan.notification.repository.EmitterRepository;
import basecamp.zikgwan.notification.repository.NotificationRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import java.io.IOException;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@Service
@RequiredArgsConstructor
public class SseService {

    // SSE 이벤트 타임아웃 시간
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;
    private final UserRepository userRepository;

    // sse 구독
    public SseEmitter subscribe(Long userId) {
        SseEmitter emitter = emitterRepository.save(userId, new SseEmitter(DEFAULT_TIMEOUT));

        // 사용자에게 모든 데이터가 전송되었다면 emitter 삭제
        emitter.onCompletion(() -> emitterRepository.deleteById(userId));

        // Emitter의 유효 시간이 만료되면 emitter 삭제
        // 유효 시간이 만료되었다는 것은 클라이언트와 서버가 연결된 시간동안 아무런 이벤트가 발생하지 않은 것을 의미한다.
        emitter.onTimeout(() -> emitterRepository.deleteById(userId));

        // 첫 구독시에 이벤트를 발생시킨다.
        // sse 연결이 이루어진 후, 하나의 데이터로 전송되지 않는다면 sse의 유효 시간이 만료되고 503 에러가 발생한다.
        sendToClient(userId, EventPayload.builder()
                .message("연결 완료됨 사용자 " + userId)
                .build());

        log.info("SSE 구독 확인 : {}", emitter);

        return emitter;
    }

    // 25초마다 모든 구독자에게 ping 이벤트 전송 SSE 연결 해지 방지
    @Scheduled(fixedRate = 25000)
    public void sendHeartbeat() {
        emitterRepository.findAll().forEach((userId, emitter) -> {
            try {
                emitter.send(SseEmitter.event()
                        .name("ping")
                        .data("keep-alive"));
            } catch (IOException e) {
                emitterRepository.deleteById(userId);
                log.warn("heartbeat 실패: userId={} — 연결 해제", userId);
            }
        });
    }

    // 구독한 클라이언트에게 데이터 전송
    public void broadcast(Long userId, EventPayload eventPayload) {
        sendToClient(userId, eventPayload);
    }

    private void sendToClient(Long userId, EventPayload eventPayload) {
        SseEmitter sseEmitter = emitterRepository.findById(userId);

        // DB 저장
        if (eventPayload.getRoomId() != null) {
            User receiver = userRepository.findById(userId)
                    .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

            Notification notification = Notification.builder()
                    .receiver(receiver)
                    .roomId(eventPayload.getRoomId())
                    .readAt(null)
                    .message(eventPayload.getMessage())
                    .build();

            notificationRepository.save(notification);
            log.info("알림 저장 완료: userId={}, message={}", userId, eventPayload.getMessage());
        }

        // emitter 없으면 종료
        if (sseEmitter == null) {
            log.info("userId={}는 현재 오프라인 — 알림은 DB에만 저장됨", userId);
            return;
        }

        // 실시간 전송
        try {
            String eventName = (eventPayload.getRoomId() == null)
                    ? "connect"
                    : "chat-notification";

            sseEmitter.send(SseEmitter.event()
                    .id(userId.toString())
                    .name(eventName)
                    .data(eventPayload));

            log.info("SSE 실시간 알림 전송 성공: userId={}, event={}", userId, eventName);
        } catch (IOException | IllegalStateException e) {
            // 연결 끊긴 emitter는 제거
            emitterRepository.deleteById(userId);
            log.warn("SSE 연결 끊김 — userId={}, error={}", userId, e.getMessage());
        } catch (Exception e) {
            // 기타 예외는 로그만 남김
            log.error("SSE 전송 중 예기치 못한 오류 — userId={}, error={}", userId, e.getMessage(), e);
        }
    }
}
