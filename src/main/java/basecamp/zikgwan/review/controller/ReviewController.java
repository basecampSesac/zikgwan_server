package basecamp.zikgwan.review.controller;

import basecamp.zikgwan.common.dto.ApiResponse;
import basecamp.zikgwan.config.security.CustomUserPrincipal;
import basecamp.zikgwan.review.dto.ReviewRequestDto;
import basecamp.zikgwan.review.service.ReviewService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/review")
public class ReviewController {

    private final ReviewService reviewService;

    /**
     * 거래 평가
     */
    @PostMapping("/rating/{tsId}")
    public ResponseEntity<ApiResponse<String>> createReview(@PathVariable Long tsId,
                                                            @AuthenticationPrincipal CustomUserPrincipal principal,
                                                            @Valid @RequestBody ReviewRequestDto requestDto) {
        String result = reviewService.createReview(principal.getUserId(), tsId, requestDto);

        return ResponseEntity
                .status(HttpStatus.OK)
                .body(ApiResponse.success(result));
    }

}
