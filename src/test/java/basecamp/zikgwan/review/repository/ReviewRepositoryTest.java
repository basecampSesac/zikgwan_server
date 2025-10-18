package basecamp.zikgwan.review.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.config.JpaAuditingConfig;
import basecamp.zikgwan.review.Review;
import basecamp.zikgwan.ticketsale.TicketSale;
import basecamp.zikgwan.ticketsale.enums.Seat;
import basecamp.zikgwan.ticketsale.enums.TicketState;
import basecamp.zikgwan.ticketsale.repository.TicketSaleRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import java.time.LocalDateTime;
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

    @Autowired
    private TicketSaleRepository ticketSaleRepository;

    @DisplayName("티켓 id로 저장된 모든 거래 평가를 조회한다.")
    @Test
    void findAllByTicketSaleAndSaveState() {
        // given

        User reviewer = createUser("reviewer", "user1@email.com", "*user11234", null);
        User reviewee = createUser("reviewee", "user2@email.com", "*user21234", null);

        userRepository.saveAll(List.of(reviewer, reviewee));

        TicketSale ticket = createTicketSale("title", "description", 10000, LocalDateTime.now().minusMinutes(1), 2,
                "한화", "삼성", "서울",
                Seat.Y, TicketState.END, reviewee);

        ticketSaleRepository.save(ticket);

        Review review = createReview(ticket, 4.0, reviewer, reviewee);

        reviewRepository.save(review);

        // when
        List<Review> reviews = reviewRepository.findAllByTicketSaleAndSaveState(ticket, SaveState.Y);

        // then
        assertThat(reviews).hasSize(1)
                .extracting("rating", "reviewer", "reviewee")
                .containsExactlyInAnyOrder(
                        tuple(4.0, reviewer, reviewee)
                );

    }

    private TicketSale createTicketSale(String title, String description, Integer price,
                                        LocalDateTime gameDay,
                                        Integer ticketCount,
                                        String home, String away, String stadium, Seat adjacentSeat, TicketState state,
                                        User sellerId) {
        return TicketSale.builder()
                .title(title)
                .description(description)
                .price(price)
                .gameDay(gameDay)
                .ticketCount(ticketCount)
                .home(home)
                .away(away)
                .stadium(stadium)
                .adjacentSeat(adjacentSeat)
                .state(state)
                .sellerId(sellerId)
                .build();

    }

    private Review createReview(TicketSale ticketSale, Double rating, User user1, User user2) {
        return Review.builder()
                .ticketSale(ticketSale)
                .rating(rating)
                .reviewer(user1)
                .reviewee(user2)
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