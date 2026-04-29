package maeilmail.learning.domain.course;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "course_enrollments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class CourseEnrollment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private CourseType courseType;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime startedAt;

    @Column
    private LocalDateTime endedAt;

    public static CourseEnrollment create(String userEmail, CourseType courseType) {
        CourseEnrollment enrollment = new CourseEnrollment();
        enrollment.userEmail = userEmail;
        enrollment.courseType = courseType;
        return enrollment;
    }

    public boolean isActive() {
        return endedAt == null;
    }

    public void end() {
        this.endedAt = LocalDateTime.now();
    }
}
