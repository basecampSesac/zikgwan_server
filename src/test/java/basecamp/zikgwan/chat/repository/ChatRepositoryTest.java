package basecamp.zikgwan.chat.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import basecamp.zikgwan.chat.domain.Chat;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.mongo.DataMongoTest;
import org.springframework.test.context.ActiveProfiles;

@DataMongoTest
@ActiveProfiles("mongo")
class ChatRepositoryTest {

    @Autowired
    private ChatRepository chatRepository;

    @DisplayName("채팅방 id와 입장 시간으로 입장 이후에 온 메시지를 모두 불러온다.")
    @Test
    void findAllByRoomIdAndSentAtAfter() {
        // given
        LocalDateTime joinedAt = LocalDateTime.of(2025, 10, 6, 10, 30);

        String roomId = "1";

        LocalDateTime beforeSentAt = LocalDateTime.of(2025, 10, 6, 10, 29);
        LocalDateTime afterSentAt = LocalDateTime.of(2025, 10, 6, 10, 31);

        Chat beforeChat = createChat(roomId, "name", "메시지1", beforeSentAt);
        Chat afterChat = createChat(roomId, "nick", "메시지2", afterSentAt);

        chatRepository.saveAll(List.of(beforeChat, afterChat));

        // when
        List<Chat> chats = chatRepository.findAllByRoomIdAndSentAtAfter(roomId, joinedAt);

        // then
        assertThat(chats).hasSize(1)
                .extracting("sender", "message", "sentAt")
                .containsExactlyInAnyOrder(
                        tuple(afterChat.getSender(), afterChat.getMessage(), afterChat.getSentAt())
                );
    }

    @DisplayName("입장 시간 이전의 채팅들은 조회할 수 없다.")
    @Test
    void findAllByRoomIdAndSentAtBefore() {
        // given
        LocalDateTime joinedAt = LocalDateTime.of(2025, 10, 6, 10, 30);

        String roomId = "1";

        LocalDateTime beforeSentAt = LocalDateTime.of(2025, 10, 6, 10, 29);

        Chat beforeChat = createChat(roomId, "name", "메시지1", beforeSentAt);

        chatRepository.save(beforeChat);

        // when
        List<Chat> chats = chatRepository.findAllByRoomIdAndSentAtAfter(roomId, joinedAt);

        // then
        assertThat(chats).isEmpty();
    }

    @DisplayName("roomId로 채팅방의 모든 채팅을 삭제한다.")
    @Test
    void deleteAllByRoomId() {
        // given

        String roomId = "1";

        LocalDateTime sentAt = LocalDateTime.now();

        LocalDateTime joinedAt = sentAt.minusHours(1);

        Chat chat1 = createChat(roomId, "name", "메시지1", sentAt);
        Chat chat2 = createChat(roomId, "name", "메시지2", sentAt);
        Chat chat3 = createChat(roomId, "name", "메시지3", sentAt);

        chatRepository.saveAll(List.of(chat1, chat2, chat3));

        // when
        chatRepository.deleteAllByRoomId(roomId);

        // then
        List<Chat> chats = chatRepository.findAllByRoomIdAndSentAtAfter(roomId, joinedAt);

        assertThat(chats).isEmpty();

    }

//    private User createUser(Long userId, String nickname, String email, SaveState saveState) {
//        return User.builder()
//                .userId(userId)
//                .nickname(nickname)
//                .email(email)
//                .saveState(saveState)
//                .build();
//    }
//
//    private ChatRoom createChatRoom(Long roomId, String roomName, RoomType roomType) {
//        return ChatRoom.builder()
//                .roomId(roomId)
//                .roomName(roomName)
//                .type(roomType)
//                .build();
//    }
//
//    private ChatRoomUser createChatRoomUser(User user, LocalDateTime joinedAt) {
//        return ChatRoomUser.builder()
//                .user(user)
//                .joinedAt(joinedAt)
//                .build();
//    }

    private Chat createChat(String roomId, String sender, String message, LocalDateTime sentAt) {
        return Chat.builder()
                .roomId(roomId)
                .sender(sender)
                .message(message)
                .sentAt(sentAt)
                .build();

    }


}