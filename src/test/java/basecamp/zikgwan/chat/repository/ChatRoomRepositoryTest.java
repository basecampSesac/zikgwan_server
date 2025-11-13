package basecamp.zikgwan.chat.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import basecamp.zikgwan.chat.domain.ChatRoom;
import basecamp.zikgwan.chat.dto.TicketRoomCount;
import basecamp.zikgwan.chat.enums.RoomType;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.config.JpaAuditingConfig;
import basecamp.zikgwan.ticketsale.TicketSale;
import basecamp.zikgwan.ticketsale.enums.Seat;
import basecamp.zikgwan.ticketsale.enums.TicketState;
import basecamp.zikgwan.ticketsale.repository.TicketSaleRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import java.time.LocalDateTime;
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
    private UserRepository userRepository;

    @Autowired
    private ChatRoomRepository chatRoomRepository;

    @Autowired
    private TicketSaleRepository ticketSaleRepository;

    @DisplayName("채팅 타입을 통해 해당 채팅 타입의 채팅방이 많은 순으로 상위 4개를 조회한다.")
    @Test
    void findTicketSalesByChatRoomCount() {
        // given
        PageRequest limit = PageRequest.of(0, 4);

        User seller = createUser("seller", "user1@email.com", "*user11234", null);

        userRepository.save(seller);

        ChatRoom room1 = createChatRoom("room1", RoomType.T, 1L);
        room1.updateSaveState(SaveState.Y);
        ChatRoom room2 = createChatRoom("room2", RoomType.T, 1L);
        room2.updateSaveState(SaveState.Y);
        ChatRoom room3 = createChatRoom("room3", RoomType.T, 1L);
        room3.updateSaveState(SaveState.Y);
        ChatRoom room4 = createChatRoom("room4", RoomType.T, 2L);
        room4.updateSaveState(SaveState.Y);
        ChatRoom room5 = createChatRoom("room5", RoomType.T, 2L);
        room5.updateSaveState(SaveState.Y);
        ChatRoom room6 = createChatRoom("room6", RoomType.T, 3L);
        room6.updateSaveState(SaveState.Y);
        ChatRoom room7 = createChatRoom("room7", RoomType.T, 3L);
        room7.updateSaveState(SaveState.Y);
        ChatRoom room8 = createChatRoom("room8", RoomType.T, 3L);
        room8.updateSaveState(SaveState.Y);
        ChatRoom room9 = createChatRoom("room9", RoomType.T, 3L);
        room9.updateSaveState(SaveState.Y);
        ChatRoom room10 = createChatRoom("room10", RoomType.C, 4L);
        room10.updateSaveState(SaveState.Y);
        ChatRoom room11 = createChatRoom("room11", RoomType.C, 4L);
        room11.updateSaveState(SaveState.Y);
        ChatRoom room12 = createChatRoom("room12", RoomType.T, 5L);
        room12.updateSaveState(SaveState.Y);
        ChatRoom room13 = createChatRoom("room13", RoomType.T, 6L);
        room13.updateSaveState(SaveState.Y);

        TicketSale sale1 = createTicketSale(seller);
        TicketSale sale2 = createTicketSale(seller);
        TicketSale sale3 = createTicketSale(seller);
        TicketSale sale4 = createTicketSale(seller);
        TicketSale sale5 = createTicketSale(seller);

        ticketSaleRepository.saveAll(List.of(sale1, sale2, sale3, sale4, sale5));

        chatRoomRepository.saveAll(
                List.of(room1, room2, room3, room4, room5, room6, room7, room8, room9, room10, room11, room12, room13));

        // when
        List<TicketRoomCount> ticketSalesByChatRoomCount = chatRoomRepository.findTicketSalesByChatRoomCount(RoomType.T,
                SaveState.Y, limit);

        // then
        assertThat(ticketSalesByChatRoomCount).hasSize(4)
                .extracting("tsId", "chatRoomCount")
                .containsExactlyInAnyOrder(
                        tuple(1L, 3L),
                        tuple(2L, 2L),
                        tuple(3L, 4L),
                        tuple(5L, 1L)
                );
    }

    private User createUser(String nickname, String email, String password, String club) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .password(password)
                .club(club)
                .saveState(SaveState.Y)
                .build();

    }

    private ChatRoom createChatRoom(String roomName, RoomType roomType, Long typeId) {
        return ChatRoom.builder()
                .roomName(roomName)
                .type(roomType)
                .typeId(typeId)
                .build();
    }

    private TicketSale createTicketSale(User seller) {
        return TicketSale.builder()
                .title("테스트 티켓 ")
                .description("테스트용 티켓입니다.")
                .price(10000)
                .gameDay(LocalDateTime.now())
                .ticketCount(1)
                .home("두산")
                .away("롯데")
                .stadium("잠실")
                .adjacentSeat(Seat.Y)
                .state(TicketState.ING)
                .sellerId(seller)
                .build();
    }

}