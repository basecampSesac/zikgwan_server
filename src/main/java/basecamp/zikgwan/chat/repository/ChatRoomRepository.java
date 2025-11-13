package basecamp.zikgwan.chat.repository;

import basecamp.zikgwan.chat.domain.ChatRoom;
import basecamp.zikgwan.chat.dto.TicketRoomCount;
import basecamp.zikgwan.chat.enums.RoomType;
import basecamp.zikgwan.common.enums.SaveState;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

    // 채팅 타입을 받아서 해당 채팅 타입의 채팅방이 많은 순으로 상위 4개만 조회
    @Query("""
            SELECT new basecamp.zikgwan.chat.dto.TicketRoomCount(c.typeId, COUNT(c))
            FROM ChatRoom c
            JOIN TicketSale s ON s.tsId = c.typeId
            WHERE c.type = :type
              AND c.saveState = :saveState
              AND s.state <> basecamp.zikgwan.ticketsale.enums.TicketState.END
            GROUP BY c.typeId
            ORDER BY COUNT(c) DESC
            """)
    List<TicketRoomCount> findTicketSalesByChatRoomCount(
            @Param("type") RoomType type,
            @Param("saveState") SaveState saveState,
            Pageable pageable);

    Optional<ChatRoom> findFirstByTypeIdAndType(Long typeId, RoomType roomType);

    Optional<ChatRoom> findByTypeIdAndBuyerIdAndType(Long typeId, Long buyerId, RoomType type);

    List<ChatRoom> findAllByTypeIdAndType(Long tsId, RoomType roomType);

    List<ChatRoom> findAllBySellerIdAndTypeIdAndType(Long sellerId, Long typeId, RoomType type);
}
