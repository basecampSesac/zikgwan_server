package basecamp.zikgwan.user.repository;

import basecamp.zikgwan.user.domain.Token;
import basecamp.zikgwan.user.domain.User;
import jakarta.transaction.Transactional;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface TokenRepository extends JpaRepository<Token, Long> {
    // 특정 유저의 RefreshToken 찾기
    Optional<Token> findByUser(User user);

    // refreshToken 문자열로 조회
    Optional<Token> findByRefreshToken(String refreshToken);

    Optional<Token> findByUserUserId(Long userId);

    // 특정 유저의 RefreshToken 삭제
    @Modifying
    @Transactional
    @Query("delete from Token t where t.user.id = :userId")
    void deleteByUserId(@Param("userId") Long userId);
}
