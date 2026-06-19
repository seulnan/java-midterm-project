package maeilmail.learning.domain.answer.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;

public record SubmitAnswerRequest(
        @NotNull(message = "questionId는 필수입니다.") Long questionId,
        // 빈 문자열("")은 미작성/오답으로 간주해 채점 흐름을 타도록 허용한다.
        // null만 거부(서비스의 isBlank() 호출 NPE 방지). 채점은 AnswerService에서 isBlank() 기준.
        @NotNull(message = "submittedText는 필수입니다.") String submittedText,
        @PositiveOrZero long responseTimeMs,
        @Email(message = "올바른 이메일 형식이어야 합니다.")
        @NotBlank(message = "userEmail은 필수입니다.") String userEmail
) {}
