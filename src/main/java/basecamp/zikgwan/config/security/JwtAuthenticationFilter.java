package basecamp.zikgwan.config.security;


import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component // 스프링 컨테이너한테 Bean 으로 등록해서 의존성 주입
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final TokenProvider tokenProvider;
    private final UserRepository userRepository; // DB 조회용
    private final ObjectMapper objectMapper = new ObjectMapper();


    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain filterChain)
            throws ServletException, IOException {

        // 인증 제외 경로
        String path = req.getRequestURI();
        if (path.startsWith("/api/user/login")
                || path.startsWith("/api/email/")
                || path.startsWith("/api/user/chknickname")
                || path.startsWith("/api/user/signup")
                || path.startsWith("/api/user/pwReset")
                || path.startsWith("/api/user/refresh/")
                || path.startsWith("/api/match/")
                || path.startsWith("/api/chatroom/user") && req.getMethod().equals("GET")
                || path.startsWith("/api/chatroom/chat/ticket/desc") && req.getMethod().equals("GET")
                || path.startsWith("/api/notification/")
                || path.startsWith("/sub/")
                || path.equals("/")
                || path.startsWith("/ws-connect") // WebSocket은 인증 제외
                || path.startsWith("/api/sse/")
                || path.startsWith("/api/socialLogin") //소셜로그인
                || (path.startsWith("/api/images") && req.getMethod().equals("GET")// 이미지 GET 조회만 인증 제외
                || path.startsWith("/images/") // 이미지 조회 인증 제외
                || path.startsWith("/api/review")) // 거래 평가 인증 제외
                || (path.startsWith("/api/communities") && req.getMethod().equals("GET")) // 모임 GET 조회만 인증 제외
        ) {
            filterChain.doFilter(req, res);
            return;
        }

        try {
            // req에서 token 꺼내오기
            String token = resolveToken(req);
            log.info("JwtAuthenticationFilter 실행");

            String header = req.getHeader("Authorization");

            if (header == null || !header.startsWith("Bearer ")) {
                res.setContentType("application/json");
                res.setCharacterEncoding("UTF-8");
                res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

                // ApiResponse.fail 활용
                ApiResponse<?> apiResponse = ApiResponse.fail("Authorization header is missing or token is invalid");

                // JSON 변환 후 응답
                res.getWriter().write(objectMapper.writeValueAsString(apiResponse));
                return;
            }

            // token 검사
            if (token != null && tokenProvider.validateToken(token)) {
                Long userId = tokenProvider.getUserId(token);
                log.info("토큰 user id : " + userId);

                User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("사용자 없음"));

                CustomUserPrincipal principal = new CustomUserPrincipal(user);

                AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(principal, null,
                        principal.getAuthorities());

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
            // 다음 필터/컨트롤러로 넘김
            filterChain.doFilter(req, res);

        } catch (Exception e) {
            // logger? spring security 필터 클래스에 기본 내장된 록
            logger.error("Could not set user authentication is security context", e);

        }

    }

    @Autowired
    public JwtAuthenticationFilter(TokenProvider tokenProvider, UserRepository userRepository) {
        this.tokenProvider = tokenProvider;
        this.userRepository = userRepository;
    }

    private String resolveToken(HttpServletRequest request) {
        String bearer = request.getHeader("Authorization");
        if (bearer != null && bearer.startsWith("Bearer ")) {
            return bearer.substring(7);
        }
        return null;
    }
}