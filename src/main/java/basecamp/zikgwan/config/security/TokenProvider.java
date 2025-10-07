package basecamp.zikgwan.config.security;


import basecamp.zikgwan.config.jwt.JwtProperties;
import basecamp.zikgwan.user.domain.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

// 사용자 정보를 받아 JWT 생성
@Service // 서비스 계층 컴포넌트로 등록해서 다른 곳에서 주입받아서 쓰기 위함
@Slf4j
public class TokenProvider {

    //JwtProperties 클래스 이용해 설정 파일 값 불러오기
    @Autowired
    private JwtProperties jwtProperties;

    // Refresh Token 만료시간 (7일)
    private final long REFRESH_TOKEN_EXPIRE_TIME = 1000L * 60 * 60 * 24 * 7;

    //create() : JWT 생성(로그인성공시)
    public String create(User userEntity) {
        // JWT 토크 만료 시간을 현재시각으로부터 1일뒤 만료되는 날짜로 계산
        Date expiryDate = Date.from(Instant.now().plus(1, ChronoUnit.DAYS));

        // JWT 토큰 생성
        return Jwts.builder()
                // header에 들어갈 내용 및 서명하기 위한 SECRET KEY
                .signWith(SignatureAlgorithm.HS512, jwtProperties.getSecretKey())
                // payload
                .setSubject(String.valueOf(userEntity.getUserId())) // sub : 토큰 제목( 여기서는 userId)
                .setIssuer("zikgwan app") // iss : 토큰 발급자
                .setIssuedAt(new Date()) // iat : 토큰이 발급된 시간
                .setExpiration(expiryDate) // exp : 토큰 만료 시간
                .compact(); // 토큰 생성 - "header.payload.signature" 토큰 문자열 최종 생성
    }

    //validateAndGetUserId() :  클라이언트가 보낸 토큰이 유효한지 검증하고, userId를 반환
    public String validateAndGetUserId(String token) {
        // parseClaimsJws 메소드가 Base64 로 디코딩 및 파싱
        // - header, payload 를 setSigningKey 로 넘어온 시크릿을 이용해 서명한 후 token의 서명이랑 비교
        // - 서명이 위조되거나 만료된 토큰이라면 예외 발생
        // -  위조되지 않았다면 페이로드(Claims) 리턴
        Claims claims = Jwts.parser().setSigningKey(jwtProperties.getSecretKey()) // 서명 검증에 사용할 비밀 지정
                .parseClaimsJws(token) // JWT 파싱 -> h, p, s 검증
                .getBody();

        return claims.getSubject(); // jwt 생성시 넣었던 sub(userId) 값을 꺼냄
    }

    // Refresh Token 생성
    public String createRefreshToken(User userEntity, Date expiryDate) {

        return Jwts.builder().signWith(SignatureAlgorithm.HS512, jwtProperties.getSecretKey())
                .setSubject(String.valueOf(userEntity.getUserId())).setIssuer("zikgwan app").setIssuedAt(new Date())
                .setExpiration(expiryDate).compact();
    }

    // 토큰 유효성 검증만 (만료 여부 확인)
    public boolean validateToken(String token) {
        try {
            Jwts.parser().setSigningKey(jwtProperties.getSecretKey()).parseClaimsJws(token);
            return true;
        } catch (Exception e) {
            log.error("Invalid JWT token: {}", e.getMessage());
            return false;
        }
    }

    public Long getUserId(String token) {
        Claims claims = Jwts.parser().setSigningKey(jwtProperties.getSecretKey()).parseClaimsJws(token).getBody();

        return Long.parseLong(claims.getSubject()); // subject 에 userId 넣어놨다고 가정
    }
}
