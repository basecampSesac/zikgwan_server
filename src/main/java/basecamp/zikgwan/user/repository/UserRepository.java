package basecamp.zikgwan.user.repository;

import basecamp.zikgwan.user.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    //TODO 임시로 만듦, 시큐리티 적용시 삭제
    Optional<User> findByNickname(String nickName);
}
