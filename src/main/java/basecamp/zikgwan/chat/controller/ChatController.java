package basecamp.zikgwan.chat.controller;

import basecamp.zikgwan.chat.dto.ChatDto;
import basecamp.zikgwan.chat.service.ChatService;
import java.time.LocalDateTime;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageExceptionHandler;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;

@Slf4j
@Controller
@RequiredArgsConstructor
public class ChatController {

    private final SimpMessageSendingOperations messagingTemplate;   // 동적으로 방 생성 가능
    private final ChatService chatService;

    /**
     * 최초 채팅방 입장 (메시지 전송) -> STOMP(WebSocket) 연결용
     * TODO 프로트와 연결 시 추가 설정 적용 필요
     */
    @MessageMapping("/chat.enter.{chatRoomId}")
    public void enterUser(@RequestBody ChatDto chatDto, @DestinationVariable Long chatRoomId) {

        // 사용자 채팅방 처음 입장
        chatService.enterRoom(chatRoomId, chatDto.getNickname());

        // 채팅방 입장 메시지 설정
        chatDto.setSentAt(LocalDateTime.now());
        chatDto.setNickname(chatDto.getNickname());
        chatDto.setMessage(chatDto.getNickname() + "님이 입장하셨습니다.");
        log.info("채팅방 입장 완료");

        messagingTemplate.convertAndSend("/sub/chat." + chatRoomId, chatDto);
    }

    /**
     * 메시지 전송
     */
    @MessageMapping("/chat.{chatRoomId}")
    public void sendMessage(@RequestBody ChatDto chatDto, @DestinationVariable Long chatRoomId) {

        chatDto.setSentAt(LocalDateTime.now());  // 현재 시간 저장

        log.info("채팅 내용: {}", chatDto);

        // 채팅 저장과 동시에 알림을 보냄    // TODO SSE 구독 오류로 수정 필요 예외 터져서 실시간으로 메시지 안 보임
        chatService.saveChat(chatDto, chatRoomId);

        // 해당 주소를 구독한 클라이언트에게 전송
        messagingTemplate.convertAndSend("/sub/chat." + chatRoomId, chatDto);
    }

    /**
     * WebSocket 예외 처리, 예외 메시지 확인용
     */
    @MessageExceptionHandler
    public void handleException(RuntimeException e) {
        log.info("Exception: {}", e.getMessage());
    }

}
