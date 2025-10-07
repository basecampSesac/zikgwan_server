package basecamp.zikgwan.chat.service;

import basecamp.zikgwan.chat.domain.Chat;
import basecamp.zikgwan.chat.domain.ChatRoom;
import basecamp.zikgwan.chat.domain.ChatRoomUser;
import basecamp.zikgwan.chat.dto.ChatDto;
import basecamp.zikgwan.chat.dto.ChatRoomDto;
import basecamp.zikgwan.chat.dto.ChatUserDto;
import basecamp.zikgwan.chat.dto.TicketInfoDto;
import basecamp.zikgwan.chat.dto.TicketRoomCount;
import basecamp.zikgwan.chat.dto.UserInfoDto;
import basecamp.zikgwan.chat.enums.RoomType;
import basecamp.zikgwan.chat.repository.ChatRepository;
import basecamp.zikgwan.chat.repository.ChatRoomRepository;
import basecamp.zikgwan.chat.repository.ChatRoomUserRepository;
import basecamp.zikgwan.notification.dto.EventPayload;
import basecamp.zikgwan.notification.service.SseService;
import basecamp.zikgwan.ticketsale.TicketSale;
import basecamp.zikgwan.ticketsale.repository.TicketSaleRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final TicketSaleRepository ticketSaleRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final SseService sseService;

    // 모든 채팅방 목록 불러오기
    @Transactional(readOnly = true)
    public List<ChatRoomDto> getChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream().map(c -> ChatRoomDto.builder().
                roomId(c.getRoomId())
                .roomName(c.getRoomName())
                .type(c.getType())
                .typeId(c.getTypeId())
                .userCount(c.getUserCount()).build()).collect(Collectors.toList());
    }

    // 사용자의 모든 채팅방 목록 불러오기
    //TODO 시큐리티 세팅 필요 -> userId 가져올 필요 없어짐
    @Transactional(readOnly = true)
    public List<ChatRoomDto> getUserChatRooms(Long userId) {

        List<ChatRoom> rooms = chatRoomUserRepository.findChatRoomsByUserId(userId);

        return rooms.stream().map(c -> ChatRoomDto.builder().
                roomId(c.getRoomId())
                .roomName(c.getRoomName())
                .type(c.getType())
                .typeId(c.getTypeId())
                .userCount(c.getUserCount()).build()).collect(Collectors.toList());
    }

    // 채팅방 이름으로 채팅방 생성

    /// 그룹
    @Transactional
    public ChatRoomDto createCommunityRoom(Long communityId, String roomName) {
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .type(RoomType.C)
                .typeId(communityId)
                .build();

        chatRoomRepository.save(chatRoom);

        return ChatRoomDto.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .userCount(chatRoom.getUserCount())
                .type(chatRoom.getType())
                .typeId(chatRoom.getTypeId())
                .build();
    }

    /// 티켓 채팅방
    @Transactional
    public ChatRoomDto createTicketRoom(Long tsId, String roomName) {
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .type(RoomType.T)
                .typeId(tsId)
                .build();

        chatRoomRepository.save(chatRoom);

        return ChatRoomDto.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .userCount(chatRoom.getUserCount())
                .type(chatRoom.getType())
                .typeId(chatRoom.getTypeId())
                .build();
    }

    // 채팅방에 참여한 user 조회
    @Transactional(readOnly = true)
    public List<UserInfoDto> getUsers(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        List<User> chatUsers = chatRoom.getChatRoomUsers().stream()
                .map(ChatRoomUser::getUser)
                .toList();

        return chatUsers.stream()
                .map(c -> UserInfoDto.builder()
                        .email(c.getEmail())
                        .nickname(c.getNickname())
                        .build())
                .toList();
    }

    // 처음 채팅방 입장
    // 처음 채팅방에 들어갈때만 사용하기 때문에 이후 채팅방 들어가면 호출하면 안 됨
    //TODO 시큐리티 세팅 필요 -> userId 가져올 필요 없어짐
    @Transactional
    public ChatUserDto enterRoom(Long roomId, String nickname) {

        // TODO 임시로 만듦 추후 밑에 주석 처리된 코드 사용, userId로 nickname 찾아서 넣어주면 됨
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));
//        User user = userRepository.findById(userId)
//                .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        // 이미 user가 들어간 해당 채팅방이 존재할 경우 예외 던짐
        if (chatRoomUserRepository.existsByChatRoomAndUser(chatRoom, user)) {
            throw new IllegalArgumentException("이미 방이 존재합니다.");
        }
        chatRoom.upUserCount();

        ChatRoomUser chatRoomUser = ChatRoomUser.builder()
                .user(null)
                .build();

        // 양방향 연관관계 동기화
        user.addChatRoomUser(chatRoomUser);
        chatRoom.addChatRoomUser(chatRoomUser);

        // 채팅방 입장한 상태로 변경
        user.updateCurrentRoomId(chatRoom.getRoomId());

        ChatRoomUser saveChatUser = chatRoomUserRepository.save(chatRoomUser);

        return ChatUserDto.builder()
                .roomId(saveChatUser.getChatRoom().getRoomId())
                .userId(saveChatUser.getUser().getUserId())
                .build();
    }

    // 채팅방 떠나기 (아예 나감)
    @Transactional
    public String leaveRoom(Long roomId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(() -> new NoSuchElementException("채팅방에 참여한 사용자가 존재하지 않습니다."));

        chatRoom.downUserCount();
        chatRoom.removeChatRoomUser(chatRoomUser);
        chatRoomUserRepository.delete(chatRoomUser);

        // 채팅방 입장하지 않은 상태로 변경
        user.updateCurrentRoomId(null);
        userRepository.save(user);

        // 방 참여 인원이 0명이면 채팅방도 삭제
        if (chatRoom.getUserCount() <= 0) {
            chatRoomRepository.deleteById(roomId);
            chatRepository.deleteAllByRoomId(String.valueOf(roomId));
        }

        //TODO 반환값 재설정 -> 현재 String
//        return ChatUserDto.builder()
//                .roomId(chatRoomUser.getChatRoom().getRoomId())
//                .userId(chatRoomUser.getUser().getUserId())
//                .build();

        return "채팅방 나가기 완료";
    }

    // 채팅방 들어올때마다 호출
    @Transactional
    public String joinRoom(Long roomId, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자가 존재하지 않습니다."));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        // 채팅방 입장한 상태로 변경
        user.updateCurrentRoomId(chatRoom.getRoomId());

        userRepository.save(user);

        return user.getNickname() + " 사용자 채팅방 입장 성공";
    }

    // 채팅방 나가기, 채팅방 나올때마다 호출 -> 화면에서 나가는 경우에도 호출
    @Transactional
    public String exitRoom(Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("사용자가 존재하지 않습니다."));

        // 채팅방 입장하지 않은 상태로 변경
        user.updateCurrentRoomId(null);

        userRepository.save(user);

        return user.getNickname() + " 사용자 채팅방 퇴장 성공";
    }


    // 채팅 내용 리스트
    // TODO 시큐리티 세팅 필요 -> userId 가져올 필요 없어짐
    @Transactional(readOnly = true)
    public List<ChatDto> getChatMessages(Long roomId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자가 존재하지 않습니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));
        ChatRoomUser chatRoomUser = chatRoomUserRepository.findByChatRoomAndUser(chatRoom, user)
                .orElseThrow(() -> new NoSuchElementException("채팅방에 참여한 사용자가 존재하지 않습니다."));

        // 찾은 채팅 내용
        List<Chat> chats = chatRepository.findAllByRoomIdAndSentAtAfter(String.valueOf(roomId),
                chatRoomUser.getJoinedAt());

        return chats.stream().map(c -> ChatDto.builder()
                .nickname(c.getSender())
                .message(c.getMessage())
                .sentAt(c.getSentAt())
                .build()).collect(Collectors.toList());
    }

    // 채팅 내용 저장
    @Transactional
    public void saveChat(ChatDto chatDto, Long roomId) {
        Chat chat = Chat.builder()
                .roomId(String.valueOf(roomId))
                .sender(chatDto.getNickname())
                .message(chatDto.getMessage())
                .sentAt(chatDto.getSentAt())
                .build();

        Chat savedChat = chatRepository.save(chat);

        log.info("Service 저장된 채팅: {}", savedChat);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        List<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findAllByChatRoom(chatRoom);

        for (ChatRoomUser cru : chatRoomUsers) {
            User receiver = cru.getUser();

            // 자신이 보낸 채팅은 알림 제외, 현재 채팅방에 있는 사람은 알림 제외
            if (!receiver.getNickname().equals(chatDto.getNickname()) && receiver.getCurrentRoomId()
                    .equals(chatRoom.getRoomId())) {
                sseService.broadcast(
                        receiver.getUserId(),
                        new EventPayload(roomId, chatDto.getMessage())
                );
            }
        }

    }

    // 채팅 수 내림차순으로 티켓 10개 조회
    public List<TicketInfoDto> getTicketsOrderByChatDesc() {

        // 10개 제한
        PageRequest limit = PageRequest.of(0, 10);

        List<TicketRoomCount> ticketRoomCounts =
                chatRoomRepository.findTicketSalesByChatRoomCount(RoomType.T, limit);

        List<Long> tsIds = ticketRoomCounts.stream()
                .map(TicketRoomCount::getTsId)
                .toList();

        List<TicketSale> ticketSales = ticketSaleRepository.findByTsIdIn(tsIds);

        // Map으로 최적화 (tsId를 key로)
        Map<Long, TicketSale> saleMap = ticketSales.stream()
                .collect(Collectors.toMap(TicketSale::getTsId, ts -> ts));

        return ticketRoomCounts.stream()
                .map(c -> {
                    TicketSale s = saleMap.get(c.getTsId());
                    return TicketInfoDto.builder()
                            .tsId(s.getTsId())
                            .title(s.getTitle())
                            .description(s.getDescription())
                            .price(s.getPrice())
                            .gameDay(s.getGameDay())
                            .ticketCount(s.getTicketCount())
                            .home(s.getHome())
                            .away(s.getAway())
                            .stadium(s.getStadium())
                            .adjacentSeat(s.getAdjacentSeat())
                            .state(s.getState())
                            .saveState(s.getSaveState())
                            .chatRoomCount(c.getChatRoomCount())
                            .build();
                })
                .toList();
    }

}
