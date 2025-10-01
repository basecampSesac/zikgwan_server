package basecamp.zikgwan.chat.service;

import basecamp.zikgwan.chat.domain.Chat;
import basecamp.zikgwan.chat.domain.ChatRoom;
import basecamp.zikgwan.chat.domain.ChatRoomUser;
import basecamp.zikgwan.chat.dto.ChatDto;
import basecamp.zikgwan.chat.dto.ChatRoomDto;
import basecamp.zikgwan.chat.dto.ChatUserDto;
import basecamp.zikgwan.chat.dto.UserInfoDto;
import basecamp.zikgwan.chat.enums.RoomType;
import basecamp.zikgwan.chat.repository.ChatRepository;
import basecamp.zikgwan.chat.repository.ChatRoomRepository;
import basecamp.zikgwan.chat.repository.ChatRoomUserRepository;
import basecamp.zikgwan.user.User;
import basecamp.zikgwan.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

    private final ChatRepository chatRepository;
    private final UserRepository userRepository;
    private final ChatRoomRepository chatRoomRepository;
    private final ChatRoomUserRepository chatRoomUserRepository;

    // 모든 채팅방 목록 불러오기
    @Transactional(readOnly = true)
    public List<ChatRoomDto> getChatRooms() {
        List<ChatRoom> chatRooms = chatRoomRepository.findAll();
        return chatRooms.stream().map(c -> ChatRoomDto.builder().
                roomId(c.getRoomId())
                .roomName(c.getRoomName())
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
                .userCount(c.getUserCount()).build()).collect(Collectors.toList());
    }

    // 채팅방 이름으로 채팅방 생성

    /// 그룹
    @Transactional
    public ChatRoomDto createCommunityRoom(String roomName) {
        ChatRoom chatRoom = ChatRoom.builder().roomName(roomName).userCount(0).type(RoomType.C).build();
        chatRoomRepository.save(chatRoom);
        return ChatRoomDto.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .userCount(chatRoom.getUserCount())
                .type(chatRoom.getType())
                .build();
    }

    /// 티켓 채팅방
    @Transactional
    public ChatRoomDto createTicketRoom(String roomName) {
        ChatRoom chatRoom = ChatRoom.builder().roomName(roomName).userCount(0).type(RoomType.T).build();
        chatRoomRepository.save(chatRoom);
        return ChatRoomDto.builder()
                .roomId(chatRoom.getRoomId())
                .roomName(chatRoom.getRoomName())
                .userCount(chatRoom.getUserCount())
                .type(chatRoom.getType())
                .build();
    }

    // 채팅방에 참여한 user 조회
    @Transactional(readOnly = true)
    public List<UserInfoDto> getUsers(Long roomId) {
        ChatRoom chatRoom = chatRoomRepository.findById(roomId)
                .orElseThrow(() -> new EntityNotFoundException("채팅방을 찾을 수 없습니다."));
        List<User> chatUsers = chatRoom.getChatRoomUsers().stream().map(ChatRoomUser::getUser)
                .toList();

        return chatUsers.stream().map(c -> UserInfoDto.builder()
                        .email(c.getEmail())
                        .nickname(c.getNickname())
                        .build())
                .toList();
    }

    // 채팅방 입장
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

        ChatRoomUser saveChatUser = chatRoomUserRepository.save(chatRoomUser);

        return ChatUserDto.builder()
                .roomId(saveChatUser.getChatRoom().getRoomId())
                .userId(saveChatUser.getUser().getUserId())
                .build();
    }

    // 채팅방 나가기
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
    }

}
