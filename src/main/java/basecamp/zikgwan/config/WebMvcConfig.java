package basecamp.zikgwan.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // 브라우저에서 /images/** 요청 → 실제 ./uploads/ 폴더 매핑
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:./uploads/")
                .setCachePeriod(3600) // 1시간 캐시
                .resourceChain(true);
    }
}
