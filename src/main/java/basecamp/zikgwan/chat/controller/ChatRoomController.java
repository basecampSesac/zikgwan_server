package basecamp.zikgwan.chat.controller;

import basecamp.zikgwan.chat.dto.ChatDto;
import basecamp.zikgwan.chat.dto.ChatRoomDto;
import basecamp.zikgwan.chat.dto.ChatUserDto;
import basecamp.zikgwan.chat.dto.TicketInfoDto;
import basecamp.zikgwan.chat.dto.UserInfoDto;
import basecamp.zikgwan.chat.service.ChatService;
import basecamp.zikgwan.common.dto.ApiResponse;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/chatroom")
public class ChatRoomController {

    private final ChatService chatService;

    /**
     * 사용자와 관계없이 모든 채팅방 목록 조회
     */
    @GetMapping("/")
    public ResponseEntity<ApiResponse<List<ChatRoomDto>>> getChatRooms() {
        List<ChatRoomDto> chatRoomDtos = chatService.getChatRooms();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(chatRoomDtos));
    }

    /**
     * 사용자의 모든 채팅방 목록 조회
     */
    //TODO UserId 시큐리티 설정
    @GetMapping("/{userId}")
    public ResponseEntity<ApiResponse<List<ChatRoomDto>>> getUserChatRooms(@PathVariable Long userId) {
        List<ChatRoomDto> chatRoomDtos = chatService.getUserChatRooms(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(chatRoomDtos));
    }

    /**
     * 채팅에 참여한 모든 유저 조회
     */
    @GetMapping("/user/{roomId}")
    public ResponseEntity<ApiResponse<List<UserInfoDto>>> getUsers(@PathVariable Long roomId) {
        List<UserInfoDto> userInfoDtos = chatService.getUsers(roomId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(userInfoDtos));
    }

    /**
     * 모임 채팅방 생성
     */
    @PostMapping("/community/{communityId}")
    public ResponseEntity<ApiResponse<ChatRoomDto>> createCommunityRoom(@PathVariable Long communityId,
                                                                        @RequestParam String roomName) {
        ChatRoomDto chatRoomDto = chatService.createCommunityRoom(communityId, roomName);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(chatRoomDto));
    }

    /**
     * 티켓 중고거래 채팅방 생성
     */
    @PostMapping("/ticket/{tsId}")
    public ResponseEntity<ApiResponse<ChatRoomDto>> createTicketRoom(@PathVariable Long tsId,
                                                                     @RequestParam String roomName) {
        ChatRoomDto chatRoomDto = chatService.createTicketRoom(tsId, roomName);

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(ApiResponse.success(chatRoomDto));
    }

    /**
     * 채팅방 떠나기 (아예 나감)
     */
    //TODO UserId 시큐리티 설정
    @DeleteMapping("/{roomId}/{userId}")
    public ResponseEntity<ApiResponse<String>> leaveRoom(@PathVariable Long roomId, @PathVariable Long userId) {
        String message = chatService.leaveRoom(roomId, userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(message));
    }

    /**
     * 채팅방 들어오기, 채팅방 들어올때마다 호출
     */
    // TODO 시큐리티 적용 필요
    @PatchMapping("/{roomId}/join/{userId}")
    public ResponseEntity<ApiResponse<ChatUserDto>> joinRoom(@PathVariable Long roomId, @PathVariable Long userId) {
        ChatUserDto chatUserDto = chatService.joinRoom(roomId, userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(chatUserDto));
    }

    /**
     * 채팅방 나가기, 채팅방 나갈때마다 호출
     */
    // TODO 시큐리티 적용 필요
    @PatchMapping("/exit/{userId}")
    public ResponseEntity<ApiResponse<String>> exitRoom(@PathVariable Long userId) {
        String message = chatService.exitRoom(userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(message));
    }

    /**
     * 채팅 내용 불러오기
     */
    @GetMapping("/chat/{roomId}/{userId}")

    public ResponseEntity<ApiResponse<List<ChatDto>>> getChatMessages(@PathVariable Long roomId,
                                                                      @PathVariable Long userId) {
        List<ChatDto> chatDtos = chatService.getChatMessages(roomId, userId);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(chatDtos));
    }

    /**
     * 채팅 수 내림차순으로 티켓 10개 조회
     */
    @GetMapping("/chat/ticket/desc")
    public ResponseEntity<ApiResponse<List<TicketInfoDto>>> getTicketsOrderByChatDesc() {

        List<TicketInfoDto> ticketInfoDtos = chatService.getTicketsOrderByChatDesc();

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(ticketInfoDtos));
    }

}
