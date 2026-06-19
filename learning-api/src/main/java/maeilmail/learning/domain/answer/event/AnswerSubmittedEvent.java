package maeilmail.learning.domain.answer.event;

import maeilmail.learning.domain.grading.GradeResult;

public record AnswerSubmittedEvent(
        Long answerId,
        String userEmail,
        Long questionId,
        boolean isCorrect,
        long responseTimeMs,
        GradeResult grade
) {
    /** 채점 결과가 없는 경우(통계 등 grade가 필요 없는 테스트)를 위한 편의 생성자. */
    public AnswerSubmittedEvent(Long answerId, String userEmail, Long questionId,
                                boolean isCorrect, long responseTimeMs) {
        this(answerId, userEmail, questionId, isCorrect, responseTimeMs, null);
    }
}
