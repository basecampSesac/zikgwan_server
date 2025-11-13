package basecamp.zikgwan.chat.repository;

import basecamp.zikgwan.chat.domain.Chat;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRepository extends MongoRepository<Chat, String> {

    // 채팅방 id와 입장 시간으로 입장 이후에 온 메시지 모두 불러옴
    List<Chat> findAllByRoomIdAndSentAtAfter(String roomId, LocalDateTime joinedAt);

    // 특정 채팅방의 모든 메시지 삭제
    void deleteAllByRoomId(String roomId);

}
