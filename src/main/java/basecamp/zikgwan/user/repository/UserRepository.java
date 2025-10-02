package basecamp.zikgwan.user.repository;

import basecamp.zikgwan.user.domain.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {


    //TODO 임시로 만듦, 시큐리티 적용시 삭제
    Optional<User> findByNickname(String nickName);
    User findByEmail(String email);
    Boolean existsByEmail(String email);
    Boolean existsByNickname(String nickName);
    User findByEmailAndPassword(String email, String password);

    @Modifying
    @Transactional
    @Query("update User u set u.saveState = 'N' where u.userId = :userId")
    int updateSaveStatusToN(@Param("userId") Long userId);
}
