package basecamp.zikgwan.notification.controller;

import basecamp.zikgwan.notification.dto.EventPayload;
import basecamp.zikgwan.notification.service.SseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/sse")
public class SseController {

    private final SseService sseService;

    /**
     * 클라이언트 이벤트 구독
     */
    @GetMapping(value = "/subscribe/{userId}", produces = "text/event-stream")
    public SseEmitter subscribe(@PathVariable Long userId) {
        log.info("컨트롤러 요청 확인 : {}", userId);
        return sseService.subscribe(userId);
    }

    /**
     * 알림 전송
     */
    @PostMapping("/broadcast/{userId}")
    public void broadcast(@PathVariable Long userId, @RequestBody EventPayload eventPayload) {
        sseService.broadcast(userId, eventPayload);
    }

}
