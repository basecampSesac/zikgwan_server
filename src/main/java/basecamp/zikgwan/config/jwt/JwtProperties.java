package basecamp.zikgwan.config.jwt;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Getter
@Setter
@Component
@ConfigurationProperties("jwt")// 자바 클래스에서
public class JwtProperties {

    private String issuer;
    private String secretKey;
}

// application.properties 파일에 있는 설정 값을 가져오고자 하는 클래스