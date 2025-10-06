package basecamp.zikgwan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@EnableJpaAuditing
@Profile("!mongo")  // mongo properties 에는 적용 X
public class JpaAuditingConfig {
}
