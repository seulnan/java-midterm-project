package maeilmail.learning.domain.review.dto;

import java.time.LocalDateTime;
import java.util.List;

/** 복습 화면 데이터 — 전체 시도 요약 + 오답노트 문제별 상세(내 답안·점수·피드백). */
public record ReviewResponse(
        String email,
        int totalAttempts,
        int correctCount,
        List<ReviewItem> items
) {
    public record ReviewItem(
            Long questionId,
            String title,
            String content,
            String category,
            String myAnswer,
            int score,
            List<Missed> missed,
            String modelAnswer,
            List<Integer> attemptScores,
            int attemptCount,
            LocalDateTime nextReviewAt,
            int intervalDays
    ) {}

    public record Missed(String label, String why) {}
}
