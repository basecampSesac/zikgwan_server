package basecamp.zikgwan.review.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.times;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.review.Review;
import basecamp.zikgwan.review.dto.ReviewRequestDto;
import basecamp.zikgwan.review.repository.ReviewRepository;
import basecamp.zikgwan.ticketsale.TicketSale;
import basecamp.zikgwan.ticketsale.enums.Seat;
import basecamp.zikgwan.ticketsale.enums.TicketState;
import basecamp.zikgwan.ticketsale.repository.TicketSaleRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    ReviewService reviewService;

    @Mock
    ReviewRepository reviewRepository;

    @Mock
    TicketSaleRepository ticketSaleRepository;

    @Mock
    UserRepository userRepository;

    @DisplayName("거래 평가를 생성한다.")
    @Test
    void createReview() {
        // given
        User reviewer = createUser(1L, "reviewer", "user1@email.com", "*user11234", null);
        User reviewee = createUser(2L, "reviewee", "user2@email.com", "*user21234", null);

        TicketSale ticket = createTicketSale(1L, "title", "description", 10000, LocalDateTime.now().minusMinutes(1), 2,
                "한화", "삼성", "서울",
                Seat.Y, TicketState.END, reviewee);

        ticket.updateBuyerId(reviewer.getUserId());

        ReviewRequestDto requestDto = ReviewRequestDto.builder()
                .rating(4.5)
                .build();

        given(userRepository.findById(reviewer.getUserId())).willReturn(Optional.of(reviewer));
        given(userRepository.findById(reviewee.getUserId())).willReturn(Optional.of(reviewee));
        given(ticketSaleRepository.findById(ticket.getTsId())).willReturn(Optional.of(ticket));
        given(reviewRepository.findAllByTicketSaleAndSaveState(ticket, SaveState.Y)).willReturn(List.of());

        // when
        String result = reviewService.createReview(reviewer.getUserId(), ticket.getTsId(), requestDto);
        // then
        then(reviewRepository).should(times(1)).save(any(Review.class));
        assertThat(result).isEqualTo("4.5점 리뷰 완료");
    }

    @DisplayName("판매중 거래는 평가할 수 없다.")
    @Test
    void createReviewSellingTicket() {
        // given
        User reviewer = createUser(1L, "reviewer", "user1@email.com", "*user11234", null);
        User reviewee = createUser(2L, "reviewee", "user2@email.com", "*user21234", null);

        TicketSale ticket = createTicketSale(1L, "title", "description", 10000, LocalDateTime.now().minusMinutes(1), 2,
                "한화", "삼성", "서울",
                Seat.Y, TicketState.ING, reviewee);
        ticket.updateBuyerId(reviewer.getUserId());

        ReviewRequestDto requestDto = new ReviewRequestDto(5.0);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(reviewer));
        given(ticketSaleRepository.findById(anyLong())).willReturn(Optional.of(ticket));
        given(reviewRepository.findAllByTicketSaleAndSaveState(any(), any(SaveState.class)))
                .willReturn(List.of());

        // when // then
        assertThatThrownBy(() -> reviewService.createReview(reviewer.getUserId(), ticket.getTsId(), requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("판매 중인 거래는 평가할 수 없습니다.");
    }

    @DisplayName("구매자가 아닌 사용자는 평가할 수 없다.")
    @Test
    void createReviewNotBuyer() {
        // given
        User reviewer = createUser(1L, "reviewer", "user1@email.com", "*user11234", null);
        User reviewee = createUser(2L, "reviewee", "user2@email.com", "*user21234", null);

        TicketSale ticket = createTicketSale(1L, "title", "description", 10000, LocalDateTime.now().minusMinutes(1), 2,
                "한화", "삼성", "서울",
                Seat.Y, TicketState.END, reviewee);
        ticket.updateBuyerId(999L); // 구매자 ID 다름

        ReviewRequestDto requestDto = new ReviewRequestDto(3.0);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(reviewer));
        given(ticketSaleRepository.findById(anyLong())).willReturn(Optional.of(ticket));
        given(reviewRepository.findAllByTicketSaleAndSaveState(any(), any())).willReturn(List.of());

        // when // then
        assertThatThrownBy(() -> reviewService.createReview(reviewer.getUserId(), ticket.getTsId(), requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("구매자가 아닌 사용자는 평가할 수 없습니다.");
    }

    @DisplayName("이미 리뷰가 존재하면 평가할 수 없다.")
    @Test
    void createReviewExistsReview() {
        // given
        User reviewer = createUser(1L, "reviewer", "user1@email.com", "*user11234", null);
        User reviewee = createUser(2L, "reviewee", "user2@email.com", "*user21234", null);

        TicketSale ticket = createTicketSale(1L, "title", "description", 10000, LocalDateTime.now().minusMinutes(1), 2,
                "한화", "삼성", "서울",
                Seat.Y, TicketState.END, reviewee);
        ticket.updateBuyerId(reviewer.getUserId());

        Review existingReview = createReview(1L, ticket, 4.0, reviewer, reviewee);
        ReviewRequestDto requestDto = new ReviewRequestDto(4.5);

        given(userRepository.findById(anyLong())).willReturn(Optional.of(reviewer));
        given(ticketSaleRepository.findById(anyLong())).willReturn(Optional.of(ticket));
        given(reviewRepository.findAllByTicketSaleAndSaveState(any(), any())).willReturn(List.of(existingReview));

        // when // then
        assertThatThrownBy(() -> reviewService.createReview(reviewer.getUserId(), ticket.getTsId(), requestDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이미 평가한 거래입니다.");
    }

    private Review createReview(Long reviewId, TicketSale ticketSale, Double rating, User user1, User user2) {
        return Review.builder()
                .reviewId(reviewId)
                .ticketSale(ticketSale)
                .rating(rating)
                .reviewer(user1)
                .reviewee(user2)
                .build();
    }

    private User createUser(Long userId, String nickname, String email, String password, String club) {
        return User.builder()
                .userId(userId)
                .nickname(nickname)
                .email(email)
                .password(password)
                .club(club)
                .saveState(SaveState.Y)
                .build();

    }

    private TicketSale createTicketSale(Long tsId, String title, String description, Integer price,
                                        LocalDateTime gameDay,
                                        Integer ticketCount,
                                        String home, String away, String stadium, Seat adjacentSeat, TicketState state,
                                        User sellerId) {
        return TicketSale.builder()
                .tsId(tsId)
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


}