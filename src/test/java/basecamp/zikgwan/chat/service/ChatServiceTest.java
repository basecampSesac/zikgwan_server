package basecamp.zikgwan.chat.service;

import static org.assertj.core.api.Assertions.assertThat;

import basecamp.zikgwan.chat.domain.ChatRoom;
import basecamp.zikgwan.chat.enums.RoomType;
import basecamp.zikgwan.chat.repository.ChatRoomRepository;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.community.Community;
import basecamp.zikgwan.community.repository.CommunityRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import jakarta.persistence.EntityManager;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("mongo")
@EnableJpaAuditing
class ChatServiceTest {

    @Autowired
    private ChatService chatService;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private CommunityRepository communityRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private EntityManager em;

    private ChatRoom chatRoom;
    private Community community;
    private List<User> users;

    @BeforeEach
    void setUp() {
        // 모임장 생성
        User leader = userRepository.save(User.builder()
                .nickname("leader")
                .email("leader@test.com")
                .saveState(SaveState.Y)
                .build());

        // 제한 인원 10명짜리 커뮤니티
        community = communityRepository.save(Community.builder()
                .title("동시성 테스트 모임")
                .description("Race Condition Test")
                .date(LocalDateTime.now().plusDays(1))
                .stadium("잠실")
                .home("LG")
                .away("두산")
                .memberCount(10)
                .isFull(false)
                .user(leader)
                .build());

        // 채팅방 생성
        chatRoom = chatRoomRepository.save(ChatRoom.builder()
                .roomName("동시성테스트방")
                .type(RoomType.C)
                .typeId(community.getCommunityId())
                .build());

        // 유저 100명 생성
        users = new ArrayList<>();
        for (int i = 0; i < 100; i++) {
            users.add(User.builder()
                    .nickname("user" + i)
                    .email("user" + i + "@test.com")
                    .saveState(SaveState.Y)
                    .build());
        }
        userRepository.saveAll(users);
    }

    @AfterEach
    void tearDown() {
        chatRoomRepository.deleteAll();
        communityRepository.deleteAll();
        userRepository.deleteAll();
    }

    @DisplayName("동시에 100명 입장 시 userCount가 제한 인원(10명)을 초과할 수 있다.")
    @Test
    void testConcurrentEnterCommunityRoom() throws InterruptedException {
        int totalUsers = 100;
        ExecutorService executor = Executors.newFixedThreadPool(32);
        CountDownLatch latch = new CountDownLatch(totalUsers);

        for (User user : users) {
            executor.submit(() -> {
                try {
                    // 일부 스레드에 랜덤 딜레이로 경쟁 유도
                    Thread.sleep((long) (Math.random() * 20));
                    chatService.handleCommunityRoomEnter(chatRoom, user);
                } catch (Exception ignored) {
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await();
        executor.shutdown();

        ChatRoom updatedRoom = chatRoomRepository.findById(chatRoom.getRoomId()).orElseThrow();
        Community updatedCommunity = communityRepository.findById(community.getCommunityId()).orElseThrow();

        int actual = updatedRoom.getUserCount();
        int limit = updatedCommunity.getMemberCount();

        // Race Condition 발생
        assertThat(actual)
                .as(" 제한 인원 : (%d명) 입장 인원 : (%d명)", limit, actual)
                .isEqualTo(limit);
    }
}