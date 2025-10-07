package basecamp.zikgwan.community.repository;

import basecamp.zikgwan.community.Community;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface CommunityRepository extends JpaRepository<Community, Long> {
    // JpaRepository의 기본 메서드들 사용
    // - save() : 등록/수정
    // - findById() : ID로 조회
    // - findAll() : 전체 조회
    // - deleteById() : 삭제

//    // 제목으로 검색 (대소문자 구분 없이)
//    List<Community> findByTitleContainingIgnoreCase(String title);
//
//    // 경기장으로 검색 (대소문자 구분 없이)
//    @Query("SELECT c FROM Community c JOIN c.matchSchedule ms WHERE ms.stadium LIKE %:stadium%")
//    List<Community> findByStadiumContainingIgnoreCase(@Param("stadium") String stadium);
//
//    // 팀으로 검색 (홈팀 또는 어웨이팀)
//    @Query("SELECT c FROM Community c JOIN c.matchSchedule ms WHERE ms.homeTeam LIKE %:team% OR ms.awayTeam LIKE %:team%")
//    List<Community> findByTeamContainingIgnoreCase(@Param("team") String team);
//
//    // 날짜로 검색 (하루만)
//    @Query("SELECT c FROM Community c JOIN c.matchSchedule ms WHERE ms.matchDate = :date")
//    List<Community> findByMatchDate(@Param("date") LocalDate date);
//
//    // 모임장 닉네임으로 검색
//    @Query("SELECT c FROM Community c JOIN c.leader u WHERE u.nickname LIKE %:nickname%")
//    List<Community> findByLeaderNicknameContainingIgnoreCase(@Param("nickname") String nickname);


    // 경기 제목, 모임 구단, 구장, 경기 날짜 선택 조회
    @Query("SELECT c FROM Community c " +
            "WHERE (:title IS NULL OR c.title LIKE %:title%) " +
            "AND (:team IS NULL OR LOWER(c.home) LIKE LOWER(CONCAT('%', :team, '%'))\n " +
            "OR LOWER(c.away) LIKE LOWER(CONCAT('%', :team, '%'))) " +
            "AND (:stadium IS NULL OR c.stadium = :stadium) " +
            "AND (:date IS NULL OR c.date = :date)")
    List<Community> searchCommunitiesByTitleAndTeamAndStadiumAndDate(
            @Param("title") String title,
            @Param("team") String team,
            @Param("stadium") String stadium,
            @Param("date") LocalDateTime date);
}
