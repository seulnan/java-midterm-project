package maeilmail.learning.domain.course.dto;

import java.time.LocalDateTime;
import maeilmail.learning.domain.course.CourseEnrollment;
import maeilmail.learning.domain.course.CourseType;

public record CourseEnrollmentDto(
        Long id,
        String userEmail,
        CourseType courseType,
        LocalDateTime startedAt,
        boolean active
) {
    public static CourseEnrollmentDto from(CourseEnrollment enrollment) {
        return new CourseEnrollmentDto(
                enrollment.getId(),
                enrollment.getUserEmail(),
                enrollment.getCourseType(),
                enrollment.getStartedAt(),
                enrollment.isActive()
        );
    }
}
