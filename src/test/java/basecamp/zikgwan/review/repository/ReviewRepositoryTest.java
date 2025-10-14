package basecamp.zikgwan.review.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.config.JpaAuditingConfig;
import basecamp.zikgwan.review.Review;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@Import(JpaAuditingConfig.class)
@DataJpaTest
@ActiveProfiles("test")
@Transactional
class ReviewRepositoryTest {

    @Autowired
    private ReviewRepository reviewRepository;

    @Autowired
    private UserRepository userRepository;

    @DisplayName("티켓 id로 저장된 모든 거래 평가를 조회한다.")
    @Test
    void findAllByRefIdAndSaveState() {
        // given

        User user1 = createUser("user1", "user1@email.com", "*user11234", null);
        User user2 = createUser("user2", "user2@email.com", "*user21234", null);

        userRepository.saveAll(List.of(user1, user2));

        Long tsId = 1L;

        Review review = createReview(tsId, 3.0, user1, user2);

        reviewRepository.save(review);

        // when
        List<Review> reviews = reviewRepository.findAllByRefIdAndSaveState(tsId, SaveState.Y);

        // then
        assertThat(reviews).hasSize(1)
                .extracting("refId", "rating", "reviewer", "reviewee")
                .containsExactlyInAnyOrder(
                        tuple(1L, 3.0, user1, user2)
                );

    }

    private Review createReview(Long tsId, Double rating, User user1, User user2) {
        return Review.builder()
                .refId(tsId)
                .rating(rating)
                .reviewer(user1)
                .reviewee(user2)
                .saveState(SaveState.Y)
                .build();
    }

    private User createUser(String nickname, String email, String password, String club) {
        return User.builder()
                .nickname(nickname)
                .email(email)
                .password(password)
                .club(club)
                .saveState(SaveState.Y)
                .build();

    }

}