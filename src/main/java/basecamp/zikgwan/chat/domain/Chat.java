package basecamp.zikgwan.chat.domain;


import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Document(collection = "chat_message")
public class Chat {

    @Id
    private String id;

    @Field(name = "room_id")
    private String roomId;

    @Field(name = "sender_id")
    private String senderId;

    @Field(name = "message")
    private String message;

    @Field(name = "sent_at")
    @CreatedDate    // 메세지 생성 시간 (보낸 시간)
    private LocalDateTime sentAt;
}
