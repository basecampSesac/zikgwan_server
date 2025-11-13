package basecamp.zikgwan.chat.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import basecamp.zikgwan.chat.domain.ChatRoom;
import basecamp.zikgwan.chat.domain.ChatRoomUser;
import basecamp.zikgwan.chat.enums.RoomType;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.config.JpaAuditingConfig;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


@Import(JpaAuditingConfig.class)
@DataJpaTest
@ActiveProfiles("test")
@Transactional
class ChatRoomUserRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ChatRoomUserRepository chatRoomUserRepository;

    @DisplayName("userId를 통해 사용자가 속한 모든 채팅방을 조회한다.")
    @Test
    void findChatRoomsByUserId() {
        // given

        LocalDateTime joinedAt = LocalDateTime.of(2025, 10, 11, 11, 12);

        User user = createUser("user", "user@email.com", SaveState.Y);

        userRepository.save(user);

        ChatRoom room1 = createChatRoom("room1", RoomType.T, 1L);
        ChatRoom room2 = createChatRoom("room2", RoomType.T, 1L);
        ChatRoom room3 = createChatRoom("room3", RoomType.T, 1L);
        ChatRoom room4 = createChatRoom("room4", RoomType.T, 2L);

        chatRoomRepository.saveAll(List.of(room1, room2, room3, room4));

        ChatRoomUser chatRoomUser1 = createChatRoomUser(user, room1, joinedAt);
        ChatRoomUser chatRoomUser2 = createChatRoomUser(user, room2, joinedAt.plusDays(1));
        ChatRoomUser chatRoomUser3 = createChatRoomUser(user, room3, joinedAt.plusHours(1));
        ChatRoomUser chatRoomUser4 = createChatRoomUser(user, room4, joinedAt.plusMinutes(1));

        chatRoomUserRepository.saveAll(List.of(chatRoomUser1, chatRoomUser2, chatRoomUser3, chatRoomUser4));

        // when
        List<ChatRoom> chatRooms = chatRoomUserRepository.findChatRoomsByUserId(user.getUserId());

        // then

        assertThat(user).isNotNull();

        assertThat(chatRooms).hasSize(4)
                .extracting("roomName", "typeId")
                .containsExactlyInAnyOrder(
                        tuple("room1", 1L),
                        tuple("room2", 1L),
                        tuple("room3", 1L),
                        tuple("room4", 2L)
                );

    }

    @DisplayName("사용자가 속한 채팅방이 없으면 빈 리스트를 반환한다.")
    @Test
    void findChatRoomsByUserIdWithoutEnterRoom() {
        // given

        LocalDateTime joinedAt = LocalDateTime.of(2025, 10, 11, 11, 12);

        User user1 = createUser("user1", "user1@email.com", SaveState.Y);
        User user2 = createUser("user2", "user2@email.com", SaveState.Y);

        userRepository.saveAll(List.of(user1, user2));

        ChatRoom room1 = createChatRoom("room1", RoomType.T, 1L);
        ChatRoom room2 = createChatRoom("room2", RoomType.T, 1L);
        ChatRoom room3 = createChatRoom("room3", RoomType.T, 1L);
        ChatRoom room4 = createChatRoom("room4", RoomType.T, 2L);

        chatRoomRepository.saveAll(List.of(room1, room2, room3, room4));

        ChatRoomUser chatRoomUser1 = createChatRoomUser(user2, room1, joinedAt);
        ChatRoomUser chatRoomUser2 = createChatRoomUser(user2, room2, joinedAt.plusDays(1));
        ChatRoomUser chatRoomUser3 = createChatRoomUser(user2, room3, joinedAt.plusHours(1));
        ChatRoomUser chatRoomUser4 = createChatRoomUser(user2, room4, joinedAt.plusMinutes(1));

        chatRoomUserRepository.saveAll(List.of(chatRoomUser1, chatRoomUser2, chatRoomUser3, chatRoomUser4));

        // when
        List<ChatRoom> chatRooms = chatRoomUserRepository.findChatRoomsByUserId(user1.getUserId());

        // then

        assertThat(user1).isNotNull();
        assertThat(user2).isNotNull();

        assertThat(chatRooms).isEmpty();

    }


    private User createUser(String nickname, String email, SaveState saveState) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .saveState(saveState)
                .build();
    }

    private ChatRoom createChatRoom(String roomName, RoomType roomType, Long typeId) {
        return ChatRoom.builder()
                .roomName(roomName)
                .type(roomType)
                .typeId(typeId)
                .build();
    }

    private ChatRoomUser createChatRoomUser(User user, ChatRoom chatRoom, LocalDateTime joinedAt) {
        return ChatRoomUser.builder()
                .user(user)
                .chatRoom(chatRoom)
                .joinedAt(joinedAt)
                .build();
    }


}