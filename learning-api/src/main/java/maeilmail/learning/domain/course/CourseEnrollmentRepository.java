package maeilmail.learning.domain.course;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseEnrollmentRepository extends JpaRepository<CourseEnrollment, Long> {

    Optional<CourseEnrollment> findByUserEmailAndEndedAtIsNull(String userEmail);
}
