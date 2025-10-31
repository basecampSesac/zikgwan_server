package basecamp.zikgwan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.retry.annotation.EnableRetry;

// 재시도 설정
@Configuration
@EnableRetry
public class RetryConfig {
}
