package basecamp.zikgwan.config.slack;

import java.util.Collections;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Slf4j
@Component
public class SlackNotifier {

    @Value("${slack.webhook.url}")
    private String slackWebhookUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public void send(String message) {
        try {
            Map<String, String> payload = Collections.singletonMap("text", message);

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);

            HttpEntity<Map<String, String>> entity = new HttpEntity<>(payload, headers);
            restTemplate.postForEntity(slackWebhookUrl, entity, String.class);

            log.info("=== Slack 알림 전송 완료: {}", message);
        } catch (Exception e) {
            log.error("=== Slack 알림 전송 실패: {}", e.getMessage(), e);
        }
    }

}
