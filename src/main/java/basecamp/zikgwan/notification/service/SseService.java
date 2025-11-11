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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;
import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Counter;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;


@Slf4j
@Service
//@RequiredArgsConstructor
public class SseService {

    // SSE 이벤트 타임아웃 시간
    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private final NotificationRepository notificationRepository;
    private final EmitterRepository emitterRepository;
    private final UserRepository userRepository;
    private final MeterRegistry meterRegistry;

    private final Timer notificationSaveTimer;
    private final Timer ssePushTimer;
    private final Counter sseSendCounter;
    private final Counter ssePushErrorCounter;

    @Autowired
    public SseService(NotificationRepository notificationRepository,
                      EmitterRepository emitterRepository,
                      UserRepository userRepository,
                      MeterRegistry meterRegistry) {

        this.notificationRepository = notificationRepository;
        this.emitterRepository = emitterRepository;
        this.userRepository = userRepository;
        this.meterRegistry = meterRegistry;

        this.notificationSaveTimer = Timer.builder("sse_notification_save_seconds")
                .description("Time spent saving notifications to DB")
                .register(meterRegistry);

        this.ssePushTimer = Timer.builder("sse_push_latency_seconds")
                .description("Time taken to push SSE events to clients")
                .register(meterRegistry);

        this.sseSendCounter = Counter.builder("sse_events_sent_total")
                .description("Total SSE events sent successfully")
                .register(meterRegistry);

        this.ssePushErrorCounter = Counter.builder("sse_push_errors_total")
                .description("Total SSE push errors")
                .register(meterRegistry);
    }


    // sse 구독
    public SseEmitter subscribe(Long userId) {
        // SSE Emitter 생성 및 등록 (기본 타임아웃: 1시간)
        SseEmitter emitter = emitterRepository.save(userId, new SseEmitter(DEFAULT_TIMEOUT));

        // 사용자에게 모든 데이터가 전송되었다면 emitter 삭제
        emitter.onCompletion(() -> emitterRepository.deleteById(userId));

        // Emitter의 유효 시간이 만료되면 emitter 삭제
        // 유효 시간이 만료되었다는 것은 클라이언트와 서버가 연결된 시간동안 아무런 이벤트가 발생하지 않은 것을 의미한다.
        emitter.onTimeout(() -> emitterRepository.deleteById(userId));

        // 첫 구독 시 클라이언트에 즉시 이벤트를 전송한다.
        // sendToClient() 대신 직접 emitter.send()를 호출해야 한다.
        // (emitter 초기화 직후 sendToClient() 호출 시, emitterRepository가 아직 준비되지 않아 실패 가능성이 있음)
        try {
            emitter.send(SseEmitter.event()
                    .name("connect")
                    .data("SSE 연결 성공: userId=" + userId));
            log.info("SSE 첫 이벤트 전송 성공: userId={}", userId);
        } catch (IOException e) {
            emitter.completeWithError(e);
            log.error("SSE 첫 이벤트 전송 실패: userId={}, error={}", userId, e.getMessage());
        }

        log.info("SSE 구독 확인 : {}", emitter);

        return emitter;
    }


    // 55초마다 모든 구독자에게 ping 이벤트 전송 SSE 연결 해지 방지
    @Scheduled(fixedRate = 55000)
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
//    public void broadcast(Long userId, EventPayload eventPayload) {
//        sendToClient(userId, eventPayload);
//    }
//
//    private void sendToClient(Long userId, EventPayload eventPayload) {
//        SseEmitter sseEmitter = emitterRepository.findById(userId);
//
//        // DB 저장
//        if (eventPayload.getRoomId() != null) {
//            User receiver = userRepository.findById(userId)
//                    .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));
//
//            Notification notification = Notification.builder()
//                    .receiver(receiver)
//                    .roomId(eventPayload.getRoomId())
//                    .readAt(null)
//                    .message(eventPayload.getMessage())
//                    .senderNickname(eventPayload.getNickname())
//                    .build();
//
//            notificationRepository.save(notification);
//            log.info("알림 저장 완료: userId={}, message={}", userId, eventPayload.getMessage());
//        }
//
//        // emitter 없으면 종료
//        if (sseEmitter == null) {
//            log.info("userId={}는 현재 오프라인 — 알림은 DB에만 저장됨", userId);
//            return;
//        }
//
//        // 실시간 전송
//        try {
//            String eventName = (eventPayload.getRoomId() == null)
//                    ? "connect"
//                    : "chat-notification";
//
//            sseEmitter.send(SseEmitter.event()
//                    .id(userId.toString())
//                    .name(eventName)
//                    .data(eventPayload));
//
//            log.info("SSE 실시간 알림 전송 성공: userId={}, event={}", userId, eventName);
//        } catch (IOException | IllegalStateException e) {
//            // 연결 끊긴 emitter는 제거
//            emitterRepository.deleteById(userId);
//            log.warn("SSE 연결 끊김 — userId={}, error={}", userId, e.getMessage());
//        } catch (Exception e) {
//            // 기타 예외는 로그만 남김
//            log.error("SSE 전송 중 예기치 못한 오류 — userId={}, error={}", userId, e.getMessage(), e);
//        }
//    }

    // 클라이언트에게 알림 전송
    public void broadcast(Long userId, EventPayload payload) {
        // 알림 저장
        saveNotification(userId, payload);

        // 실시간 전송
        pushToClient(userId, payload);
    }

    // 알림 DB 저장
    @Async("notificationExecutor")
    @Timed(value = "sse_notification_save_seconds", description = "Time spent saving notification to DB")
    @Transactional
    public void saveNotification(Long userId, EventPayload payload) {
        if (payload.getRoomId() == null) {
            return;
        }

        User receiver = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

        Notification notification = Notification.builder()
                .receiver(receiver)
                .roomId(payload.getRoomId())
                .readAt(null)
                .message(payload.getMessage())
                .senderNickname(payload.getNickname())
                .build();

        notificationRepository.save(notification);
        log.info("알림 저장 완료: userId={}, message={}", userId, payload.getMessage());
    }

    // SSE 실시간 전송
    @Async("notificationExecutor")
    public void pushToClient(Long userId, EventPayload payload) {
        SseEmitter emitter = emitterRepository.findById(userId);

        if (emitter == null) {
            log.info("userId={}는 현재 오프라인 — 알림은 DB에만 저장됨", userId);
            return;
        }

        ssePushTimer.record(() -> {
            try {
                String eventName = (payload.getRoomId() == null)
                        ? "connect"
                        : "chat-notification";

                synchronized (emitter) {
                    emitter.send(SseEmitter.event()
                            .id(userId.toString())
                            .name(eventName)
                            .data(payload));
                }

                sseSendCounter.increment();
                log.info("SSE 전송 성공: userId={}, event={}", userId, eventName);

            } catch (IOException | IllegalStateException e) {
                emitterRepository.deleteById(userId);
                ssePushErrorCounter.increment();
                log.warn("SSE 연결 끊김 — userId={}, error={}", userId, e.getMessage());
            } catch (Exception e) {
                ssePushErrorCounter.increment();
                log.error("SSE 전송 중 예외 — userId={}, error={}", userId, e.getMessage());
            }
        });
    }


}
