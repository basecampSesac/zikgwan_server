package basecamp.zikgwan.config;

import basecamp.zikgwan.common.Interceptor.StompAuthChannelInterceptor;
import basecamp.zikgwan.common.Interceptor.WebSocketHandshakeInterceptor;
import basecamp.zikgwan.common.Interceptor.WebSocketInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.ChannelRegistration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

@Configuration
@EnableWebSocketMessageBroker
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketMessageBrokerConfigurer {

    private final WebSocketInterceptor webSocketInterceptor;
    private final WebSocketHandshakeInterceptor handshakeInterceptor;
    private final StompAuthChannelInterceptor stompAuthChannelInterceptor;

    // 메시지가 전송되는 시점에 가로챌 수 있음
    @Override
    public void configureClientInboundChannel(ChannelRegistration registration) {
        registration.interceptors(webSocketInterceptor);    // 인터셉터 적용
        registration.interceptors(stompAuthChannelInterceptor); // STOMP 전용 jwt 인증 인터셉터 적용
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry config) {
        // 스프링의 인메모리 메시지 브로커 사용 설정
        config.enableSimpleBroker("/sub");    // 구독 주소 prefix 설정
        config.setApplicationDestinationPrefixes("/pub");   // 메시지 발행 주소 prefix 설정
    }

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint("/ws-connect")  // 초기 핸드셰이크 과정에서 사용할 endpoint 지정
//                .addInterceptors(
//                        handshakeInterceptor)    // HTTP -> WebSocket 세션이 열리기 전에 인터셉터 적용 인증 적용 가능 (인증 실패하면 WebSocket 연결 차단)
                .setAllowedOriginPatterns("*")    // CORS 허용 설정
                .withSockJS();  // SockJS 폴백 지원 (웹소켓 사용 X일 경우 Long Polling 등의 다른 방식 사용)
    }
}
