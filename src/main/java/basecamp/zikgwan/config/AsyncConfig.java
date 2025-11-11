package basecamp.zikgwan.config;

import java.util.concurrent.Executor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "notificationExecutor")
    public Executor notificationExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(8);    // 항상 유지되는 기본 스레드 수 (8개)
        executor.setMaxPoolSize(20);    // 동시에 실행할 수 있는 최대 스레드 수
        executor.setQueueCapacity(100); // 대기열 크기 — 실행 중인 스레드가 모두 바쁠 때 여기에 작업이 쌓임
        executor.setThreadNamePrefix("SSE-Async-"); // 생성되는 스레드 이름 접두사 → 로그에 표시될 때 유용
        executor.initialize();  // 스레드 풀 초기화 — 실제 동작 가능한 상태로 준비시킴
        return executor;
    }
}
