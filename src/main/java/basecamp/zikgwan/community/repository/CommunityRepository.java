package basecamp.zikgwan.community.repository;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.community.Community;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {

    // 최신순
    Page<Community> findAllBySaveStateOrderByCreatedAtDesc(SaveState saveState, Pageable pageable);

    // 모임 인원 많은 순
    Page<Community> findAllBySaveStateOrderByMemberCountDesc(SaveState saveState, Pageable pageable);

    // 모임 인원 적은 순
    Page<Community> findAllBySaveStateOrderByMemberCountAsc(SaveState saveState, Pageable pageable);

    // 경기 제목, 모임 구단, 구장, 경기 날짜 선택 조회
    @Query("SELECT c FROM Community c " +
            "WHERE (:title IS NULL OR c.title LIKE %:title%) " +
            "AND (:team IS NULL OR LOWER(c.home) LIKE LOWER(CONCAT('%', :team, '%'))\n " +
            "OR LOWER(c.away) LIKE LOWER(CONCAT('%', :team, '%'))) " +
            "AND (:stadium IS NULL OR c.stadium = :stadium) " +
            "AND (:date IS NULL OR (c.date >= :date AND c.date < :datePlusOne))")
    List<Community> searchCommunitiesByTitleAndTeamAndStadiumAndDate(
            @Param("title") String title,
            @Param("team") String team,
            @Param("stadium") String stadium,
            @Param("date") LocalDateTime date,
            @Param("datePlusOne") LocalDateTime datePlusOne);
}
