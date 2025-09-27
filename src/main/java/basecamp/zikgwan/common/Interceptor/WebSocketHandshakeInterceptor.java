package basecamp.zikgwan.common.Interceptor;

import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

/**
 * 시큐리티 적용 시 사용할 인터셉터
 * 최초 HTTP로 WebSocket 연결 시 적용됨
 */
@Slf4j
@Component
public class WebSocketHandshakeInterceptor implements HandshakeInterceptor {

    // 핸드쉐이크 전 실행
    // jwt 인증 넣을 수 있음
    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                                   Map<String, Object> attributes) throws Exception {

        // 현재 SecurityContext에서 Authentication 가져오기
//        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

//        if (authentication != null) {
//            attributes.put("authentication", authentication); // WebSocket 세션에 저장
//        }

        return true; // 핸드셰이크 허용
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler,
                               Exception exception) {

    }
}
