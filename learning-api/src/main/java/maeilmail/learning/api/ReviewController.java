package maeilmail.learning.api;

import lombok.RequiredArgsConstructor;
import maeilmail.learning.common.ApiResponse;
import maeilmail.learning.domain.review.ReviewService;
import maeilmail.learning.domain.review.dto.ReviewResponse;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/review")
@RequiredArgsConstructor
public class ReviewController {

    private final ReviewService reviewService;

    /** 복습 화면 데이터 — 전체 시도 요약 + 오답노트 문제별 내 답안·점수·피드백. */
    @GetMapping("/me")
    public ApiResponse<ReviewResponse> myReview(@RequestParam String email) {
        return ApiResponse.ok(reviewService.getReview(email));
    }
}
