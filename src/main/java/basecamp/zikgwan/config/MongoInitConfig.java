package basecamp.zikgwan.config;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MongoInitConfig {

    private final MongoTemplate mongoTemplate;

    // 환경변수 가져옴
    @Value("${spring.data.mongodb.database}")
    private String collection;

    // 해당 이름의 컬렉션(테이블)이 없으면 자동으로 생성
    @PostConstruct
    public void init() {
        if (!mongoTemplate.collectionExists(collection)) {
            mongoTemplate.createCollection(collection);
        }
    }
}
