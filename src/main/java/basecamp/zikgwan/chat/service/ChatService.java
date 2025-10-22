package basecamp.zikgwan.chat.service;

import basecamp.zikgwan.chat.domain.Chat;
import basecamp.zikgwan.chat.domain.ChatRoom;
import basecamp.zikgwan.chat.domain.ChatRoomUser;
import basecamp.zikgwan.chat.dto.ChatDto;
import basecamp.zikgwan.chat.dto.ChatRoomDto;
import basecamp.zikgwan.chat.dto.ChatUserDto;
import basecamp.zikgwan.chat.dto.NotificationChatRoomDto;
import basecamp.zikgwan.chat.dto.TicketInfoDto;
import basecamp.zikgwan.chat.dto.TicketRoomCount;
import basecamp.zikgwan.chat.dto.UserInfoDto;
import basecamp.zikgwan.chat.enums.RoomType;
import basecamp.zikgwan.chat.repository.ChatRepository;
import basecamp.zikgwan.chat.repository.ChatRoomRepository;
import basecamp.zikgwan.chat.repository.ChatRoomUserRepository;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.community.Community;
import basecamp.zikgwan.community.repository.CommunityRepository;
import basecamp.zikgwan.image.enums.ImageType;
import basecamp.zikgwan.image.service.ImageService;
import basecamp.zikgwan.notification.dto.EventPayload;
import basecamp.zikgwan.notification.service.SseService;
import basecamp.zikgwan.ticketsale.TicketSale;
import basecamp.zikgwan.ticketsale.repository.TicketSaleRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
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
    private final ImageService imageService;

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

    // 구매자의 티켓 거래 채팅방 불러오기
    public ChatRoomDto getTicketChatRoom(Long tsId, Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

        // 구매자가 참여하고 있는 채팅방 불러옴
        ChatRoom chatRoom = chatRoomRepository.findByTypeIdAndBuyerIdAndType(tsId, userId, RoomType.T)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        return ChatRoomDto.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .typeId(tsId)
                .type(chatRoom.getType())
                .userCount(chatRoom.getUserCount())
                .build();
    }

    // 채팅방 상세조회
    public NotificationChatRoomDto getChatRoomDetail(Long roomId) {

        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        return NotificationChatRoomDto.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
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
        // 로그인한 사용자 존재 확인
        userRepository.findById(userId).orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

        TicketSale ticketSale = ticketSaleRepository.findById(tsId)
                .orElseThrow(() -> new NoSuchElementException("티켓 거래를 찾을 수 없습니다."));

        ChatRoom chatRoom = ChatRoom.builder()
                .roomName(roomName)
                .type(RoomType.T)
                .typeId(tsId)
                .sellerId(ticketSale.getSellerId().getUserId())
                .buyerId(userId)
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
                        .club(u.getClub())
                        .imageUrl(imageService.getImage(ImageType.U, u.getUserId()))
                        .build())
                .toList();
    }

    // 처음 채팅방 입장
    // 처음 채팅방에 들어갈때만 사용하기 때문에 이후 채팅방 들어가면 호출하면 안 됨
    @Transactional
    public ChatUserDto enterRoom(Long roomId, String nickname) {
        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

        ChatRoom baseRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new NoSuchElementException("채팅방이 존재하지 않습니다."));

        // 이미 참여 중이면 상태만 갱신하고 리턴
        if (chatRoomUserRepository.existsByChatRoomAndUser(baseRoom, user)) {
            return joinRoom(baseRoom.getRoomId(), user.getUserId());
        }

        ChatRoom targetRoom = baseRoom; // 실제로 입장/저장 처리할 방

        if (baseRoom.getType() == RoomType.T) {
            final Long tsId = baseRoom.getTypeId();

            // 구매자 진입
            if (!Objects.equals(baseRoom.getSellerId(), user.getUserId())) {
                Long buyerId = user.getUserId();

                // 동일 티켓 + buyerId 조합의 방이 이미 있는지 확인
                Optional<ChatRoom> existedRoomOpt =
                        chatRoomRepository.findByTypeIdAndBuyerIdAndType(tsId, buyerId, RoomType.T);

                if (existedRoomOpt.isPresent()) {
                    // 즉시 return 하지 말고, 그 방으로 입장/저장 처리 계속
                    targetRoom = existedRoomOpt.get();
                } else {
                    // 현재 방이 buyer 미지정이면 이 방을 이 buyer에게 귀속
                    if (baseRoom.getBuyerId() == null) {
                        baseRoom.updateBuyerId(buyerId);
                        chatRoomRepository.save(baseRoom);
                        targetRoom = baseRoom;
                    } else {
                        // (옵션) 다른 buyer로 이미 귀속된 방이면 정책에 따라
                        // - 새 방 생성 or
                        // - 그 buyer의 방을 찾아 targetRoom으로 설정
                        // 여기서는 방을 새로 만드는 대신 오류로 처리하거나, 정책 맞게 구현
                        throw new IllegalStateException("이미 다른 구매자에게 귀속된 거래방입니다.");
                    }
                }

                // 판매자 자동 입장 보장
                TicketSale sale = ticketSaleRepository.findById(tsId)
                        .orElseThrow(() -> new NoSuchElementException("티켓 거래 정보를 찾을 수 없습니다."));
                User seller = sale.getSellerId();
                if (!chatRoomUserRepository.existsByChatRoomAndUser(targetRoom, seller)) {
                    createChatRoomUserIfAbsent(targetRoom, seller);
                    targetRoom.upUserCount();
                    chatRoomRepository.save(targetRoom);
                    log.info("판매자 {} 자동 입장 완료", seller.getNickname());
                }

                // 구매자 입장 처리 (userCount 증가 + currentRoomId 갱신)
                handleTicketRoomEnter(targetRoom, user);
            } else {
                // 판매자 진입
                handleTicketRoomEnter(targetRoom, user);
            }
        } else {
            // 모임방
            handleCommunityRoomEnter(targetRoom, user);
        }

        // 참여자 기록 (중복 방지)
        createChatRoomUserIfAbsent(targetRoom, user);

        return ChatUserDto.builder()
                .roomId(targetRoom.getRoomId())
                .userId(user.getUserId())
                .build();
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

    public List<TicketInfoDto> getTicketsOrderByChatDesc() {

        PageRequest limit = PageRequest.of(0, 4);

        List<TicketRoomCount> ticketRoomCounts = chatRoomRepository.findTicketSalesByChatRoomCount(
                RoomType.T, SaveState.Y, limit);

        List<Long> tsIds = ticketRoomCounts.stream()
                .map(TicketRoomCount::getTsId)
                .toList();

        List<TicketSale> ticketSales = ticketSaleRepository.findByTsIdIn(tsIds);

        // Map으로 최적화 (tsId → TicketSale)
        Map<Long, TicketSale> saleMap = ticketSales.stream()
                .collect(Collectors.toMap(TicketSale::getTsId, ts -> ts));

        // 각 티켓별로 이미지 URL을 조회해 Dto에 포함
        return ticketRoomCounts.stream()
                .map(c -> {
                    TicketSale s = saleMap.get(c.getTsId());
                    if (s == null) {
                        return null;
                    }

                    String imageUrl = imageService.getImage(ImageType.T, s.getTsId());

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
                            .imageUrl(imageUrl)
                            .build();
                })
                .filter(Objects::nonNull)
                .toList();
    }

    // 모임 채팅방 입장 처리
    @Transactional
    public void handleCommunityRoomEnter(ChatRoom chatRoom, User user) {
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
    @Transactional
    public void handleTicketRoomEnter(ChatRoom chatRoom, User user) {
        chatRoom.upUserCount();
        chatRoomRepository.save(chatRoom);

        user.updateCurrentRoomId(chatRoom.getRoomId());
        userRepository.save(user);

        log.info("[티켓 거래 채팅방] {} 입장 완료. 현재 인원: {}", user.getNickname(), chatRoom.getUserCount());
    }

    // ChatRoomUser 엔티티 생성 및 양방향 관계 설정
    @Transactional
    public ChatRoomUser createChatRoomUser(ChatRoom chatRoom, User user) {
        ChatRoomUser chatRoomUser = ChatRoomUser.builder()
                .user(user)
                .chatRoom(chatRoom)
                .joinedAt(LocalDateTime.now())
                .build();

        ChatRoomUser saved = chatRoomUserRepository.save(chatRoomUser);
        log.info("[ChatRoomUser] user={}, roomId={} 연결 완료", user.getNickname(), chatRoom.getRoomId());

        return saved;
    }

    @Transactional
    public ChatRoomUser createChatRoomUserIfAbsent(ChatRoom chatRoom, User user) {
        if (chatRoomUserRepository.existsByChatRoomAndUser(chatRoom, user)) {
            // 이미 있으면 아무것도 안 함
            return chatRoomUserRepository.findByChatRoomAndUser(chatRoom, user).get();
        }
        ChatRoomUser cru = ChatRoomUser.builder()
                .user(user)
                .chatRoom(chatRoom)
                .joinedAt(LocalDateTime.now())
                .build();
        return chatRoomUserRepository.save(cru);
    }
}
