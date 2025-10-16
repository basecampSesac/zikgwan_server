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
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@EqualsAndHashCode(callSuper = true)
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

    @Column(name = "type_id", nullable = false)
    private Long typeId;  // 티켓 중고거래 id 또는 모임 id

    // 티켓 거래 관련 필드 (RoomType.T에만 적용)
    @Column(name = "seller_id", nullable = true)
    private Long sellerId;

    @Column(name = "buyer_id", nullable = true)
    private Long buyerId;

    @Column(name = "unique_key", unique = true, length = 100, nullable = true)
    private String uniqueKey;   // tsId + seller_id + buyer_id 조합

    // 인원수는 채팅방 생성시 0이며 함수를 통해 ++
    @ColumnDefault("'0'")
    @Column(name = "user_count", nullable = false)
    private Integer userCount = 0;

    @OneToMany(mappedBy = "chatRoom", cascade = CascadeType.REMOVE, orphanRemoval = true)
    private List<ChatRoomUser> chatRoomUsers = new ArrayList<>();

    // 인원 수 증가
    public void upUserCount() {
        this.userCount++;
    }

    // 인원 수 감소
    public void downUserCount() {
        if (this.userCount <= 0) {    // 음수 방지
            throw new IllegalArgumentException("채팅방에 사용자가 존재하지 않습니다.");
        }
        this.userCount--;
    }

    public void updateBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public void updateUniqueKey(String uniqueKey) {
        this.uniqueKey = uniqueKey;
    }

    // unique 키 생성 메서드
    public String generateTicketUniqueKey(Long tsId, Long sellerId, Long buyerId) {
        return "TICKET_" + tsId + "_SELLER_" + sellerId + "_BUYER_" + buyerId;
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

    @Builder
    private ChatRoom(Long roomId, String roomName, RoomType type, Long typeId, Long sellerId, Long buyerId,
                     String uniqueKey) {
        this.roomId = roomId;
        this.roomName = roomName;
        this.type = type;
        this.typeId = typeId;
        this.sellerId = sellerId;
        this.buyerId = buyerId;
        this.uniqueKey = uniqueKey;
        this.userCount = 0;
    }
}
