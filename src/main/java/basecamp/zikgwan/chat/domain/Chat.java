package basecamp.zikgwan.chat.domain;


import jakarta.persistence.Id;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Builder;
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

    @Field(name = "sender") // 사용자 닉네임
    private String sender;

    @Field(name = "message")
    private String message;

    @Field(name = "sent_at")
    @CreatedDate    // 메세지 생성 시간 (보낸 시간)
    private LocalDateTime sentAt;

    @Builder
    public Chat(String roomId, String sender, String message, LocalDateTime sentAt) {
        this.roomId = roomId;
        this.sender = sender;
        this.message = message;
        this.sentAt = sentAt;
    }
}
