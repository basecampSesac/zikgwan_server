package basecamp.zikgwan.config.security;


import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.io.IOException;
import org.springframework.web.filter.OncePerRequestFilter;

@Slf4j
@Component // 스프링 컨테이너한테 Bean 으로 등록해서 의존성 주입
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private  TokenProvider tokenProvider;

    @Override
    protected  void doFilterInternal(HttpServletRequest req, HttpServletResponse res,
                                     FilterChain filterChain) throws ServletException, IOException {
        try{
            // req에서 token 꺼내오기
            String token = parseBearerToken(req);
            log.info("JwtAuthenticationFilter is running...");

            // token 검사
            if(token != null && !token.equalsIgnoreCase("null")){
                String userId = tokenProvider.validateAndGetUserId(token);
                log.info("Authenticated user id : " + userId);

                // 직전에 추출한 userId 로 인증 객체 생성
                AbstractAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userId,
                        null, AuthorityUtils.NO_AUTHORITIES);
                authentication.setDetails((new WebAuthenticationDetailsSource().buildDetails(req)));

                // SecurityContextHolder :  Spring Security 에서 인증된 사용자 정보를 저장하는 곳
                SecurityContext securityContext = SecurityContextHolder.createEmptyContext();
                securityContext.setAuthentication(authentication); // 컨텍스트에 authentication 으로 심으면 이후부터는 인증된 사용자
                SecurityContextHolder.setContext(securityContext);
            }
        }catch (Exception e){
            // logger? spring security 필터 클래스에 기본 내장된 록
            logger.error("Could not set user authentication is security context", e);

        }

        // 다음 필터/컨트롤러로 넘김
        filterChain.doFilter(req, res);
    }

    // 요청의 헤더에서 Bearer 토큰을 가져옴
    // http 요청의 헤더를 파싱해서 Bearer 토큰을 리턴
    private String parseBearerToken(HttpServletRequest req){
        // Authorixation: Bearer <token>
        String bearerToken = req.getHeader("Authorization");

        if(StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {// 조건식의 마지막 공백 추가 오타 아님!!
            return bearerToken.substring(7); // "Bearer " 문자열의 길이가 7
        }
        return null;
    }
}
