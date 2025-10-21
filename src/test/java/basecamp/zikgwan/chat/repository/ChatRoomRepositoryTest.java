package basecamp.zikgwan.chat.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import basecamp.zikgwan.chat.domain.ChatRoom;
import basecamp.zikgwan.chat.dto.TicketRoomCount;
import basecamp.zikgwan.chat.enums.RoomType;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.config.JpaAuditingConfig;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


@Import(JpaAuditingConfig.class)
@DataJpaTest
@ActiveProfiles("test")
@Transactional
class ChatRoomRepositoryTest {

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @DisplayName("채팅 타입을 통해 해당 채팅 타입의 채팅방이 많은 순으로 상위 10개를 조회한다.")
    @Test
    void findTicketSalesByChatRoomCount() {
        // given
        PageRequest limit = PageRequest.of(0, 10);

        ChatRoom room1 = createChatRoom("room1", RoomType.T, 1L);
        ChatRoom room2 = createChatRoom("room2", RoomType.T, 1L);
        ChatRoom room3 = createChatRoom("room3", RoomType.T, 1L);
        ChatRoom room4 = createChatRoom("room4", RoomType.T, 2L);
        ChatRoom room5 = createChatRoom("room5", RoomType.T, 2L);
        ChatRoom room6 = createChatRoom("room6", RoomType.T, 3L);
        ChatRoom room7 = createChatRoom("room7", RoomType.T, 3L);
        ChatRoom room8 = createChatRoom("room8", RoomType.T, 3L);
        ChatRoom room9 = createChatRoom("room9", RoomType.T, 3L);
        ChatRoom room10 = createChatRoom("room10", RoomType.C, 4L);
        ChatRoom room11 = createChatRoom("room11", RoomType.C, 4L);
        ChatRoom room12 = createChatRoom("room12", RoomType.T, 5L);
        ChatRoom room13 = createChatRoom("room13", RoomType.T, 6L);

        chatRoomRepository.saveAll(
                List.of(room1, room2, room3, room4, room5, room6, room7, room8, room9, room10, room11, room12, room13));

        // when
        List<TicketRoomCount> ticketSalesByChatRoomCount = chatRoomRepository.findTicketSalesByChatRoomCount(RoomType.T,
                limit, SaveState.Y);

        // then
        assertThat(ticketSalesByChatRoomCount).hasSize(5)
                .extracting("tsId", "chatRoomCount")
                .containsExactlyInAnyOrder(
                        tuple(1L, 3L),
                        tuple(2L, 2L),
                        tuple(3L, 4L),
                        tuple(5L, 1L),
                        tuple(6L, 1L)
                );
    }

    private ChatRoom createChatRoom(String roomName, RoomType roomType, Long typeId) {
        return ChatRoom.builder()
                .roomName(roomName)
                .type(roomType)
                .typeId(typeId)
                .build();
    }

}