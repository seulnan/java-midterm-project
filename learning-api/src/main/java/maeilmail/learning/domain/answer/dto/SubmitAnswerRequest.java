package maeilmail.learning.domain.answer.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SubmitAnswerRequest(
        @NotNull(message = "questionId는 필수입니다.") Long questionId,
        @NotBlank(message = "submittedText는 필수입니다.") String submittedText,
        @PositiveOrZero long responseTimeMs,
        @NotBlank(message = "userEmail은 필수입니다.") String userEmail
) {}
