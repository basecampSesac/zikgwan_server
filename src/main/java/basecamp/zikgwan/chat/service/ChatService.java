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
import basecamp.zikgwan.community.Community;
import basecamp.zikgwan.community.repository.CommunityRepository;
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
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final TicketSaleRepository ticketSaleRepository;
    private final CommunityRepository communityRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;
    private final SseService sseService;

    // 모든 채팅방 목록 불러오기
    public List<ChatRoomDto> getChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream()
                .map(c -> ChatRoomDto.builder().roomId(c.getRoomId()).roomName(c.getRoomName()).type(c.getType())
                        .typeId(c.getTypeId()).userCount(c.getUserCount()).build()).collect(Collectors.toList());
    }

    // 사용자의 모든 채팅방 목록 불러오기
    public List<ChatRoomDto> getUserChatRooms(Long userId) {

        List<ChatRoom> rooms = chatRoomUserRepository.findChatRoomsByUserId(userId);

        return rooms.stream()
                .map(c -> ChatRoomDto.builder().roomId(c.getRoomId()).roomName(c.getRoomName()).type(c.getType())
                        .typeId(c.getTypeId()).userCount(c.getUserCount()).build()).collect(Collectors.toList());
    }

    // 모임의 해당 채팅방 불러오기
    public ChatRoomDto getCommunityChatRoom(Long typeId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

        ChatRoom chatRoom = chatRoomRepository.findFirstByTypeIdAndType(typeId, RoomType.C)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        return ChatRoomDto.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .typeId(typeId)
                .type(chatRoom.getType())
                .userCount(chatRoom.getUserCount())
                .build();

    }

    // 채팅방 이름으로 채팅방 생성

    // 그룹
    @Transactional
    public ChatRoomDto createCommunityRoom(Long communityId, String roomName, Long userId) {
        // 로그인한 사용자 존재 확인
        userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

        ChatRoom chatRoom = ChatRoom.builder().roomName(roomName).type(RoomType.C).typeId(communityId).build();

        chatRoomRepository.save(chatRoom);

        return ChatRoomDto.builder().roomId(chatRoom.getRoomId()).roomName(chatRoom.getRoomName())
                .userCount(chatRoom.getUserCount()).type(chatRoom.getType()).typeId(chatRoom.getTypeId()).build();
    }

    // 티켓 채팅방
    @Transactional
    public ChatRoomDto createTicketRoom(Long tsId, String roomName, Long userId) {
        userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

        // 구매자와 uniqueKey 없음
        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .type(RoomType.T)
                .typeId(tsId)
                .sellerId(userId)
                .build();

        chatRoomRepository.save(chatRoom);

        return ChatRoomDto.builder().roomId(chatRoom.getRoomId()).roomName(chatRoom.getRoomName())
                .userCount(chatRoom.getUserCount()).type(chatRoom.getType()).typeId(chatRoom.getTypeId()).build();
    }

    // 채팅방에 참여한 user 조회
    public List<UserInfoDto> getUsers(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        // User를 미리 fetch join으로 가져오기
        List<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findAllWithUserByChatRoom(chatRoom);

        return chatRoomUsers.stream()
                .map(cru -> cru.getUser())
                .map(u -> UserInfoDto.builder()
                        .email(u.getEmail())
                        .nickname(u.getNickname())
                        .build())
                .toList();
    }

    // 처음 채팅방 입장
    // 처음 채팅방에 들어갈때만 사용하기 때문에 이후 채팅방 들어가면 호출하면 안 됨
    @Transactional
    public ChatUserDto enterRoom(Long roomId, String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        // 이미 참여 중인 경우 재입장 처리
        if (chatRoomUserRepository.existsByChatRoomAndUser(chatRoom, user)) {
            return joinRoom(roomId, user.getUserId());
        }

        // 티켓 거래방이라면 buyerId 및 uniqueKey 생성
        if (chatRoom.getType() == RoomType.T) {
            // 판매자의 id가 아니면 구매자의 id
            if (!Objects.equals(chatRoom.getSellerId(), user.getUserId())) {
                // 구매자 입장
                Long buyerId = user.getUserId();

                // 이미 동일 거래에 대해 생성된 방이 있는지 확인
                String candidateKey = chatRoom.generateTicketUniqueKey(
                        chatRoom.getTypeId(), chatRoom.getSellerId(), buyerId);

                Optional<ChatRoom> existedRoom = chatRoomRepository.findByUniqueKey(candidateKey);
                if (existedRoom.isPresent()) {
                    log.info("[재사용] 기존 티켓 거래방 존재: {}", candidateKey);
                    return joinRoom(existedRoom.get().getRoomId(), user.getUserId());
                }

                // 기존 방이 없으면 현재 방에 buyerId와 uniqueKey 등록
                chatRoom.updateBuyerId(buyerId);
                chatRoom.updateUniqueKey(candidateKey);
                chatRoomRepository.save(chatRoom);
                log.info("[신규 등록] 티켓 거래 uniqueKey 생성: {}", candidateKey);
            }

            handleTicketRoomEnter(chatRoom, user);
        } else {
            handleCommunityRoomEnter(chatRoom, user);
        }

        // ChatRoomUser 생성 및 관계 설정
        ChatRoomUser chatRoomUser = createChatRoomUser(chatRoom, user);

        return ChatUserDto.builder().roomId(chatRoomUser.getChatRoom().getRoomId())
                .userId(chatRoomUser.getUser().getUserId()).build();
    }

    // 채팅방 떠나기 (아예 나감)
    @Transactional
    public String leaveRoom(Long roomId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

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

        if (chatRoom.getType() == RoomType.C) {
            Community community = communityRepository.findById(chatRoom.getTypeId())
                    .orElseThrow(() -> new NoSuchElementException("모임이 존재하지 않습니다."));

            // 인원 꽉 찼는지 확인 후 상태 업데이트
            if (chatRoom.getUserCount() < community.getMemberCount()) {
                community.updateIsFull(false);
                communityRepository.save(community);
            }
        }

        // 방 참여 인원이 0명이면 채팅방도 삭제
        if (chatRoom.getUserCount() <= 0) {
            chatRoomRepository.deleteById(roomId);
            chatRepository.deleteAllByRoomId(String.valueOf(roomId));
        }

        return "채팅방 나가기 완료";
    }

    // 채팅방 들어올때마다 호출
    @Transactional
    public ChatUserDto joinRoom(Long roomId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        // 채팅방 입장한 상태로 변경
        user.updateCurrentRoomId(chatRoom.getRoomId());

        userRepository.save(user);

        return ChatUserDto.builder().roomId(chatRoom.getRoomId()).userId(user.getUserId()).build();
    }

    // 채팅방 나가기, 채팅방 나올때마다 호출 -> 화면에서 나가는 경우에도 호출
    @Transactional
    public String exitRoom(Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자가 존재하지 않습니다."));

        // 채팅방 입장하지 않은 상태로 변경
        user.updateCurrentRoomId(null);

        userRepository.save(user);

        return user.getNickname() + " 사용자 채팅방 퇴장 성공";
    }


    // 채팅 내용 리스트
    public List<ChatDto> getChatMessages(Long roomId, Long userId) {
        User user = userRepository.findById(userId).orElseThrow(() -> new EntityNotFoundException("사용자가 존재하지 않습니다."));
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        // 처음 채팅방을 생성할 경우 빈 리스트 반환
        Optional<ChatRoomUser> chatRoomUserOpt = chatRoomUserRepository.findByChatRoomAndUser(chatRoom, user);
        if (chatRoomUserOpt.isEmpty()) {
            log.info("{} 사용자가 채팅 방을 생성함 (빈 리스트 반환)", user.getNickname(), roomId);
            return List.of();
        }

        ChatRoomUser chatRoomUser = chatRoomUserOpt.get();

        List<Chat> chats = chatRepository.findAllByRoomIdAndSentAtAfter(String.valueOf(roomId),
                chatRoomUser.getJoinedAt());

        return chats.stream()
                .map(c -> ChatDto.builder().nickname(c.getSender()).message(c.getMessage()).sentAt(c.getSentAt())
                        .build()).collect(Collectors.toList());
    }

    // 채팅 내용 저장
    @Transactional
    public void saveChat(ChatDto chatDto, Long roomId) {
        Chat chat = Chat.builder().roomId(String.valueOf(roomId)).sender(chatDto.getNickname())
                .message(chatDto.getMessage()).sentAt(chatDto.getSentAt()).build();

        Chat savedChat = chatRepository.save(chat);

        log.info("Service 저장된 채팅: {}", savedChat);

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        // 해당 채팅방에 들어와 있는 사용자 정보 불러오고
        List<ChatRoomUser> chatRoomUsers = chatRoomUserRepository.findAllByChatRoom(chatRoom);

        for (ChatRoomUser cru : chatRoomUsers) {
            User receiver = cru.getUser();

            // 본인 메시지는 제외
            if (receiver.getNickname().equals(chatDto.getNickname())) {
                continue;
            }

            // 현재 채팅방에 들어와 있으면 제외
            Long currentRoomId = receiver.getCurrentRoomId();
            Long thisRoomId = chatRoom.getRoomId();

            if (Objects.equals(currentRoomId, thisRoomId)) {
                log.info("{}는 현재 방({})에 있으므로 알림 제외", receiver.getNickname(), thisRoomId);
                continue;
            }

            // 나머지 사용자에게만 알림 전송
            log.info("{}에게 알림 전송 (현재방={}, 유저방={})", receiver.getNickname(), thisRoomId, currentRoomId);
            sseService.broadcast(receiver.getUserId(),
                    EventPayload.builder().roomId(roomId).message(chatDto.getMessage()).nickname(chatDto.getNickname())
                            .sentAt(chatDto.getSentAt()).build());
        }

    }

    // 채팅 수 내림차순으로 티켓 10개 조회
    public List<TicketInfoDto> getTicketsOrderByChatDesc() {

        // 10개 제한
        PageRequest limit = PageRequest.of(0, 10);

        List<TicketRoomCount> ticketRoomCounts = chatRoomRepository.findTicketSalesByChatRoomCount(RoomType.T, limit);

        List<Long> tsIds = ticketRoomCounts.stream().map(TicketRoomCount::getTsId).toList();

        List<TicketSale> ticketSales = ticketSaleRepository.findByTsIdIn(tsIds);

        // Map으로 최적화 (tsId를 key로)
        Map<Long, TicketSale> saleMap = ticketSales.stream().collect(Collectors.toMap(TicketSale::getTsId, ts -> ts));

        return ticketRoomCounts.stream().map(c -> {
            TicketSale s = saleMap.get(c.getTsId());
            return TicketInfoDto.builder().tsId(s.getTsId()).title(s.getTitle()).description(s.getDescription())
                    .price(s.getPrice()).gameDay(s.getGameDay()).ticketCount(s.getTicketCount()).home(s.getHome())
                    .away(s.getAway()).stadium(s.getStadium()).adjacentSeat(s.getAdjacentSeat()).state(s.getState())
                    .saveState(s.getSaveState()).chatRoomCount(c.getChatRoomCount()).build();
        }).toList();
    }

    // 모임 채팅방 입장 처리
    private void handleCommunityRoomEnter(ChatRoom chatRoom, User user) {
        Community community = communityRepository.findById(chatRoom.getTypeId())
                .orElseThrow(() -> new NoSuchElementException("모임이 존재하지 않습니다."));

        // 입장 제한
        if (community.getIsFull()) {
            throw new IllegalArgumentException("채팅방 인원이 다 찼습니다.");
        }

        // 인원 증가
        chatRoom.upUserCount();

        // 인원 꽉 찼는지 확인 후 상태 업데이트
        if (chatRoom.getUserCount() >= community.getMemberCount()) {
            community.updateIsFull(true);
            communityRepository.save(community);
        }

        chatRoomRepository.save(chatRoom);
        user.updateCurrentRoomId(chatRoom.getRoomId());
        userRepository.save(user);

        log.info("[모임 채팅방] {} 입장 완료. 현재 인원: {}", user.getNickname(), chatRoom.getUserCount());
    }

    // 티켓 거래 채팅방 입장 처리
    private void handleTicketRoomEnter(ChatRoom chatRoom, User user) {
        chatRoom.upUserCount();
        chatRoomRepository.save(chatRoom);

        user.updateCurrentRoomId(chatRoom.getRoomId());
        userRepository.save(user);

        log.info("[티켓 거래 채팅방] {} 입장 완료. 현재 인원: {}", user.getNickname(), chatRoom.getUserCount());
    }

    // ChatRoomUser 엔티티 생성 및 양방향 관계 설정
    private ChatRoomUser createChatRoomUser(ChatRoom chatRoom, User user) {
        ChatRoomUser chatRoomUser = ChatRoomUser.builder().build();

        // 양방향 관계 설정
        user.addChatRoomUser(chatRoomUser);
        chatRoom.addChatRoomUser(chatRoomUser);

        ChatRoomUser savedChatRoomUser = chatRoomUserRepository.save(chatRoomUser);
        log.info("[ChatRoomUser] user={}, roomId={} 연결 완료", user.getNickname(), chatRoom.getRoomId());

        return savedChatRoomUser;
    }

}
