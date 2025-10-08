package basecamp.zikgwan.common.Interceptor;

import basecamp.zikgwan.config.security.TokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class StompAuthChannelInterceptor implements ChannelInterceptor {

    private final TokenProvider tokenProvider;

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor =
                MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            String token = accessor.getFirstNativeHeader("Authorization");

            if (token != null && token.startsWith("Bearer ")) {
                token = token.substring(7);
                if (tokenProvider.validateToken(token)) {
                    Long userId = tokenProvider.getUserId(token);

                    // Authentication 생성
                    UsernamePasswordAuthenticationToken auth =
                            new UsernamePasswordAuthenticationToken(userId, null, null);

                    accessor.setUser(auth);
                    log.info("STOMP CONNECT 인증 성공 - userId: {}", userId);
                } else {
                    log.warn("STOMP CONNECT JWT 검증 실패");
                    throw new IllegalArgumentException("STOMP CONNECT JWT 검증 실패");
                }
            } else {
                log.warn("STOMP CONNECT Authorization 헤더 없음");
                throw new IllegalArgumentException("STOMP CONNECT Authorization 헤더 없음");
            }
        }

        return message;
    }
}