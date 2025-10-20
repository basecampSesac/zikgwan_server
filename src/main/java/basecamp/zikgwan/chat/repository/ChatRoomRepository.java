package basecamp.zikgwan.chat.repository;

import basecamp.zikgwan.chat.domain.ChatRoom;
import basecamp.zikgwan.chat.dto.TicketRoomCount;
import basecamp.zikgwan.chat.enums.RoomType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 채팅 타입을 받아서 해당 채팅 타입의 채팅방이 많은 순으로 상위 10개만 조회
    @Query("SELECT new basecamp.zikgwan.chat.dto.TicketRoomCount(c.typeId, COUNT(c)) " +
            "FROM ChatRoom c " +
            "WHERE c.type = :type " +
            "GROUP BY c.typeId " +
            "ORDER BY COUNT(c) DESC")
    List<TicketRoomCount> findTicketSalesByChatRoomCount(@Param("type") RoomType type, Pageable pageable);

    Optional<ChatRoom> findFirstByTypeIdAndType(Long typeId, RoomType roomType);

    Optional<ChatRoom> findByTypeIdAndBuyerIdAndType(Long typeId, Long buyerId, RoomType type);
}
