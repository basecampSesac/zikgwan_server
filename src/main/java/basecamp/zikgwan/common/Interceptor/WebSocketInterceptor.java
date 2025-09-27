package basecamp.zikgwan.common.Interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WebSocketInterceptor implements ChannelInterceptor {

    // 보내기 전에
    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        // 메시지를 STOMP 헤더로 변환
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        // 어떤 STOMP 명령인지 확인
        StompCommand command = accessor.getCommand();

        // 구독 요청이면 구독하려는 목적지 확인
        if (command == StompCommand.SUBSCRIBE) {
            log.info(accessor.getDestination());
        }

        // 메시지 반환
        return message;
    }
}

