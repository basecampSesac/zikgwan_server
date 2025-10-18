package basecamp.zikgwan.ticketsale.repository;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.ticketsale.TicketSale;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TicketSaleRepository extends JpaRepository<TicketSale, Long> {
    List<TicketSale> findByTsIdIn(List<Long> tsIds);

    // 최신순
    Page<TicketSale> findAllBySaveStateOrderByCreatedAtDesc(SaveState saveState, Pageable pageable);

    // 티켓 가격 높은 순
    Page<TicketSale> findAllBySaveStateOrderByPriceDesc(SaveState saveState, Pageable pageable);

    // 티켓 가격 낮은 순
    Page<TicketSale> findAllBySaveStateOrderByPriceAsc(SaveState saveState, Pageable pageable);

    // 경기 제목, 모임 구단, 구장, 경기 날짜 선택 조회
    @Query("SELECT t FROM TicketSale t " +
            "WHERE (:title IS NULL OR t.title LIKE %:title%) " +
            "AND (:team IS NULL OR LOWER(t.home) LIKE LOWER(CONCAT('%', :team, '%'))\n " +
            "OR LOWER(t.away) LIKE LOWER(CONCAT('%', :team, '%'))) " +
            "AND (:stadium IS NULL OR t.stadium = :stadium) " +
            "AND (:gameDay IS NULL OR (t.gameDay >= :gameDay AND t.gameDay < :datePlusOne))")
    List<TicketSale> searchTicketSalesByTitleAndTeamAndStadiumAndGameDay(
            @Param("title") String title,
            @Param("team") String team,
            @Param("stadium") String stadium,
            @Param("gameDay") LocalDateTime gameDay,
            @Param("datePlusOne") LocalDateTime datePlusOne,
            @Param("saveState") SaveState saveState);

    // 거래 완료된 판매자 or 구매자 거래 조회
    @Query("""
            SELECT t
            FROM TicketSale t
            WHERE t.state = 'END'
              AND (t.sellerId.userId = :userId OR t.buyerId = :userId)
              AND t.saveState = 'Y'
            ORDER BY t.updatedAt DESC
            """)
    List<TicketSale> findCompletedSalesByUser(@Param("userId") Long userId);

}
