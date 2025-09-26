package basecamp.zikgwan.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.client.WebClient;

/**
 * WebConfig 설정
 */
@Configuration
public class WebClientConfig {

    @Value("${kbo.data.url}")
    private String url;

    @Bean
    public WebClient webClient(WebClient.Builder builder) {
        return builder.baseUrl(url)
                .defaultHeaders(httpHeaders -> {
                    httpHeaders.add(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
                })
                .build();
    }
}
