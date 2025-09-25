package basecamp.zikgwan.user;

import basecamp.zikgwan.common.domain.BaseEntity;
import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.group.Community;
import basecamp.zikgwan.review.Review;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.ColumnDefault;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)  // JPA용 기본 생성자
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "nickname", length = 50, unique = true, nullable = false)
    private String nickname;

    @Column(name = "email", length = 100, unique = true, nullable = false)
    private String email;

    @Column(name = "password", length = 255)
    private String password;

    @Column(name = "club", length = 50)
    private String club;

    @Enumerated(EnumType.STRING)
    @ColumnDefault("'Y'")
    @Column(name = "save_state", nullable = false)
    private SaveState saveState;

    // 남긴 리뷰들
    @OneToMany(mappedBy = "reviewer")
    private List<Review> writtenReviews = new ArrayList<>();

    // 받은 리뷰
    @OneToMany(mappedBy = "reviewee")
    private List<Review> receiveReviews = new ArrayList<>();

    // 모임 과 1:N
    @OneToMany(mappedBy = "user")
    private List<Community> communities = new ArrayList<>();
}
