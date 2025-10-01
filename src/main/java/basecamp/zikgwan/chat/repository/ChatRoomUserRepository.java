package basecamp.zikgwan.chat.repository;

import basecamp.zikgwan.chat.domain.ChatRoom;
import basecamp.zikgwan.chat.domain.ChatRoomUser;
import basecamp.zikgwan.user.User;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomUserRepository extends JpaRepository<ChatRoomUser, Long> {

    Optional<ChatRoomUser> findByChatRoomAndUser(ChatRoom chatRoom, User user);

    boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);

    // userId로 사용자가 속한 모든 채팅방 조회 (JPQL)
    @Query("SELECT cru.chatRoom FROM ChatRoomUser cru WHERE cru.user.userId = :userId")
    List<ChatRoom> findChatRoomsByUserId(@Param("userId") Long userId);

    List<ChatRoomUser> findAllByChatRoom(ChatRoom chatRoom);
}
