package maeilmail.learning.domain.answer.event;

public record AnswerSubmittedEvent(
        Long answerId,
        String userEmail,
        Long questionId,
        boolean isCorrect,
        long responseTimeMs
) {}
