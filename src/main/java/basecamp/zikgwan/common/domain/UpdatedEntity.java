package basecamp.zikgwan.common.domain;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import java.time.LocalDateTime;
import lombok.Getter;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

/**
 * 수정일만 있는 경우에 상속 받아 사용
 */
@Getter
@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public class UpdatedEntity {

    @LastModifiedDate
    private LocalDateTime updatedAt;

}
