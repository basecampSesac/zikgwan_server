package basecamp.zikgwan.notification.repository;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.notification.Notification;
import basecamp.zikgwan.user.domain.User;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    // 삭제 여부를 포함하여 User로 모든 알림 조회
    List<Notification> findAllByReceiverAndSaveState(User receiver, SaveState saveState);

    List<Notification> findAllByRoomId(Long roomId);

    List<Notification> findAllByRoomIdIn(List<Long> roomIds);
}
