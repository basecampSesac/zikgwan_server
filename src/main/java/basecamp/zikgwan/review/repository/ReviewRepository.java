package basecamp.zikgwan.review.repository;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.review.Review;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {

    // tsId로 저장된 모든 거래 평가 조회
    List<Review> findAllByRefIdAndSaveState(Long tsId, SaveState saveState);

}
