package basecamp.zikgwan.user.domain;

import basecamp.zikgwan.chat.domain.ChatRoomUser;
import basecamp.zikgwan.common.domain.BaseEntity;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.community.Community;
import basecamp.zikgwan.ticketsale.TicketSale;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA용 기본 생성자
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "nickname", length = 50, unique = true, nullable = false)
    private String nickname;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "club", length = 50)
    private String club;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Y'")
    @Column(name = "save_state", nullable = false)
    private SaveState saveState;

    /**
     * 양방향 연관관계 지정 (기본 단방향이라 필요 시 사용)
     */

    // 남긴 리뷰들
//    @OneToMany(mappedBy = "reviewer", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Review> writtenReviews = new ArrayList<>();

    // 받은 리뷰
//    @OneToMany(mappedBy = "reviewee", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Review> receiveReviews = new ArrayList<>();

    // 모임 과 1:N
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Community> communities = new ArrayList<>();

    // 사용자가 판매한 티켓들
    @OneToMany(mappedBy = "sellerId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<TicketSale> sellingTickets = new ArrayList<>();

    // 사용자가 구매한 티켓들
//    @OneToMany(mappedBy = "buyerId", cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<TicketSale> boughtTickets = new ArrayList<>();

    // 채팅 유저의 1:N
    @OneToMany(mappedBy = "user", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ChatRoomUser> chatRoomUsers = new ArrayList<>();

    // 알림과 유저의 1:N
//    @OneToMany(mappedBy = "receiver", fetch = FetchType.LAZY, cascade = CascadeType.ALL, orphanRemoval = true)
//    private List<Notification> notifications = new ArrayList<>();

    /**
     * 연관관계 편의 메서드
     */

    // 리뷰 작성자
//    public void addWrittenReview(Review review) {
//        writtenReviews.add(review);
//        review.setReviewer(this);
//    }

    // 리뷰 수신자
//    public void addReceivedReview(Review review) {
//        receiveReviews.add(review);
//        review.setReviewee(this);
//    }

    // 모임 등록
    public void addCommunity(Community community) {
        communities.add(community);
        community.setUser(this);
    }

    // 편의 메서드

    // 판매자
    public void addSellingTicket(TicketSale ticket) {
        sellingTickets.add(ticket);
        ticket.setSeller(this);
    }

//    // 구매자
//    public void addBoughtTicket(TicketSale ticket) {
//        boughtTickets.add(ticket);
//        ticket.setBuyer(this);
//    }

    // 알림
//    public void addNotification(Notification notification) {
//        notifications.add(notification);
//        notification.setUser(this);
//    }

    // 채팅 사용자 수 동기화
    public void addChatRoomUser(ChatRoomUser chatRoomUser) {
        chatRoomUsers.add(chatRoomUser);
        chatRoomUser.setUser(this); // 양방향 동기화
    }

    public void removeChatRoomUser(ChatRoomUser chatRoomUser) {
        chatRoomUsers.remove(chatRoomUser);
        chatRoomUser.setUser(null);
    }

    @Builder
    private User(Long userId, String nickname, String email, String password, String club, SaveState saveState) {
        this.userId = userId;
        this.nickname = nickname;
        this.email = email;
        this.password = password;
        this.club = club;
        this.saveState = saveState;
    }
}
