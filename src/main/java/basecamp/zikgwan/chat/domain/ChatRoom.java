package basecamp.zikgwan.chat.domain;

import basecamp.zikgwan.chat.enums.RoomType;
import basecamp.zikgwan.common.domain.CreatedEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "chat_room")
public class ChatRoom extends CreatedEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "room_id")
    private Long roomId;

    @Column(name = "room_name", length = 100)
    private String roomName;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", length = 1, nullable = false)
    private RoomType type;

    // 인원수는 채팅방 생성시 0이며 함수를 통해 ++
    @ColumnDefault("'0'")
    @Column(name = "user_count", nullable = false)
    private Integer userCount;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomUser> chatRoomUsers = new ArrayList<>();

    // 인원 수 증가
    public void upUserCount() {
        this.userCount++;
    }

    // 인원 수 감소
    public void downUserCount() {
        if (this.userCount > 0) {    // 음수 방지
            this.userCount--;
        }
    }

    /**
     * 연관관계 편의 메서드
     */
    public void addChatRoomUser(ChatRoomUser chatRoomUser) {
        chatRoomUsers.add(chatRoomUser);
        chatRoomUser.setChatRoom(this); // 양방향 동기화
    }

    public void removeChatRoomUser(ChatRoomUser chatRoomUser) {
        chatRoomUsers.remove(chatRoomUser);
        chatRoomUser.setChatRoom(null);
    }

}
