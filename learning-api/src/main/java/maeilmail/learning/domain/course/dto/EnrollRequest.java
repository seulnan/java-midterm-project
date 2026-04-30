package maeilmail.learning.domain.course.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import maeilmail.learning.domain.course.CourseType;

public record EnrollRequest(
        @Email @NotBlank(message = "userEmail은 필수입니다.") String userEmail,
        @NotNull(message = "courseType은 필수입니다.") CourseType courseType
) {}
