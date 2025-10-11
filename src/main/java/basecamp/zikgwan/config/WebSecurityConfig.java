package basecamp.zikgwan.config;


import static org.springframework.security.config.Customizer.withDefaults;

import basecamp.zikgwan.config.security.JwtAuthenticationFilter;
import java.util.Arrays;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.CsrfConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration // 스프링 컨테이너에게 해당 클래스가 Bean 정의를 포함한 설정 클래스임을 알림
@EnableWebSecurity // spring security 활성화
@Slf4j
public class WebSecurityConfig {
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http.cors(withDefaults()) // cors 기본 설정
                .csrf(CsrfConfigurer::disable) // csrf disable
                .sessionManagement(sessionManagement -> sessionManagement
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)) // sesstion 기반이 아니므로 무상태(stateless)
                /*
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/", "/api/email/**", "/api/user/login", "/api/user/chknickname",
                                "/api/user/signup", "/api/match/**")
                        .permitAll() // /, /api/email/** 경로는 인증 안해도 됨
                        .anyRequest().authenticated()); // 그 이외의 모든 경로는 인증 해야됨

                 */

                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/ws-connect/**").permitAll()   // WebSocket 핸드셰이크 HTTP 요청은 통과
                        // 정적 이미지 접근 허용
                        .requestMatchers("/images/**").permitAll()
                        .anyRequest().permitAll()); // 모든 요청 허용

        // filter 등록 :  매 요청마다 CrosFilter를 실행한 후에 JwtAuthenticationFilter 실행
        http.addFilterAfter(jwtAuthenticationFilter, CorsFilter.class);

        return http.build();
    }

    // cors 설정
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 모든 출처, 메소드, 헤더에 대해 허용하는 cors 설정
        config.setAllowCredentials(true);
        config.setAllowedOriginPatterns(Arrays.asList("*"));
        config.setAllowedMethods(Arrays.asList("HEAD", "POST", "GET", "DELETE", "PUT", "PATCH"));
        config.setAllowedHeaders((Arrays.asList("*")));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return source;
    }
}
