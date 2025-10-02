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

    // 구독한 클라이언트에게 데이터 전송
    public void broadcast(Long userId, EventPayload eventPayload) {
        sendToClient(userId, eventPayload);
    }

    private void sendToClient(Long userId, EventPayload eventPayload) {
        SseEmitter sseEmitter = emitterRepository.findById(userId);

        // roomId 없으면 DB 저장하지 않고 바로 이벤트만 전송
        if (eventPayload.getRoomId() == null) {
            try {
                sseEmitter.send(SseEmitter.event()
                        .name("connect")
                        .data(eventPayload.getMessage()));
            } catch (IOException e) {
                emitterRepository.deleteById(userId);
            }
            return;
        }

        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자를 찾을 수 없습니다."));

        Notification notification = Notification.builder()
                .receiver(receiver)
                .roomId(eventPayload.getRoomId())
                .readAt(null)   // 기본값 null (안 읽음)
                .message(eventPayload.getMessage())
                .build();

        // 알림 내용 DB 저장
        notificationRepository.save(notification);

        // sse 전송
        try {
            sseEmitter.send(
                    SseEmitter.event()
                            .id(userId.toString())
                            .name("chat-notification")
                            .data(eventPayload.getMessage())
            );
        } catch (IOException e) {
            emitterRepository.deleteById(userId);
            throw new RuntimeException("연결 오류 발생");
        }
    }
}
