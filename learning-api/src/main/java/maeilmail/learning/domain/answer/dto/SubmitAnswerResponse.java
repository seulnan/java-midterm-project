package maeilmail.learning.domain.answer.dto;

public record SubmitAnswerResponse(
        Long answerId,
        boolean isCorrect,
        int score,
        String message
) {}
