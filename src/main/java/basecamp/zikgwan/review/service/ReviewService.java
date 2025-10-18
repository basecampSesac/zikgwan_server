package basecamp.zikgwan.review.service;

import basecamp.zikgwan.common.enums.SaveState;
import basecamp.zikgwan.review.Review;
import basecamp.zikgwan.review.dto.ReviewRequestDto;
import basecamp.zikgwan.review.repository.ReviewRepository;
import basecamp.zikgwan.ticketsale.TicketSale;
import basecamp.zikgwan.ticketsale.enums.TicketState;
import basecamp.zikgwan.ticketsale.repository.TicketSaleRepository;
import basecamp.zikgwan.user.domain.User;
import basecamp.zikgwan.user.repository.UserRepository;
import java.util.List;
import java.util.NoSuchElementException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final TicketSaleRepository ticketSaleRepository;
    private final UserRepository userRepository;

    // 거래 평가 생성
    @Transactional
    public String createReview(Long userId, Long tsId, ReviewRequestDto requestDto) {

        User reviewer = userRepository.findById(userId)
                .orElseThrow(() -> new NoSuchElementException("사용자가 존재하지 않습니다."));

        TicketSale ticketSale = ticketSaleRepository.findById(tsId)
                .orElseThrow(() -> new NoSuchElementException("티켓 중고거래가 존재하지 않습니다."));

        List<Review> reviews = reviewRepository.findAllByTicketSaleAndSaveState(ticketSale, SaveState.Y);

        isValidReview(ticketSale, reviewer, reviews);

        Review review = Review.builder()
                .ticketSale(ticketSale)
                .rating(requestDto.getRating())
                .reviewee(ticketSale.getSellerId())
                .reviewer(reviewer)
                .build();

        reviewRepository.save(review);

        return review.getRating() + "점 리뷰 완료";

    }

    private void isValidReview(TicketSale ticketSale, User reviewer, List<Review> reviews) {
        if (ticketSale.getState().equals(TicketState.ING)) {
            throw new IllegalArgumentException("판매중인 거래는 평기할 수 없습니다.");
        }

        if (ticketSale.getBuyerId() != reviewer.getUserId()) {
            throw new IllegalArgumentException("구매자가 아닌 사용자는 평가할 수 없습니다.");
        }

        if (!reviews.isEmpty()) {
            throw new IllegalArgumentException("이미 평가한 거래입니다.");
        }
    }
}
