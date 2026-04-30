package maeilmail.learning.domain.answer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SubmitAnswerRequest(
        @NotNull(message = "questionId는 필수입니다.") Long questionId,
        @NotBlank(message = "submittedText는 필수입니다.") String submittedText,
        @PositiveOrZero long responseTimeMs,
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @NotBlank(message = "userEmail은 필수입니다.") String userEmail
) {}
