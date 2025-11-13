package basecamp.zikgwan.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

/**
 * restTemplate 설정
 */
@Configuration
public class RestTemplateConfig {

    @Bean
    public RestTemplate webClient() {
        return new RestTemplate();
    }
}
