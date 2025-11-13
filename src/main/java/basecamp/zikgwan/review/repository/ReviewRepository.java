package basecamp.zikgwan.review.repository;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.review.Review;
import basecamp.zikgwan.ticketsale.TicketSale;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // tsId로 저장된 모든 거래 평가 조회
    List<Review> findAllByTicketSaleAndSaveState(TicketSale ticketSale, SaveState saveState);

    // 사용자의 평균 평점 계산 및 조회
    @Query("SELECT COALESCE(AVG(r.rating), 0) " +   // 리뷰가 없으면 0
            "FROM Review r " +
            "WHERE r.reviewee.userId = :userId " +
            "AND r.saveState = :saveState")
    Double findAverageRatingByReviewee(@Param("userId") Long userId, @Param("saveState") SaveState saveState);

}
