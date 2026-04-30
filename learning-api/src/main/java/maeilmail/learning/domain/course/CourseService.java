package maeilmail.learning.domain.course;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import maeilmail.learning.domain.course.dto.CourseEnrollmentDto;
import maeilmail.learning.domain.course.dto.EnrollRequest;
import maeilmail.learning.domain.course.policy.CoursePolicy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CourseService {

    private final CourseEnrollmentRepository enrollmentRepository;
    private final List<CoursePolicy> policies;

    // Factory Map: CourseType → CoursePolicy (Strategy 패턴 디스패치)
    private Map<CourseType, CoursePolicy> policyMap() {
        return policies.stream().collect(Collectors.toMap(CoursePolicy::courseType, Function.identity()));
    }

    @Transactional
    public CourseEnrollmentDto enroll(EnrollRequest request) {
        // 기존 활성 코스 종료
        enrollmentRepository.findByUserEmailAndEndedAtIsNull(request.userEmail())
                .ifPresent(CourseEnrollment::end);

        CourseEnrollment enrollment = CourseEnrollment.create(request.userEmail(), request.courseType());
        return CourseEnrollmentDto.from(enrollmentRepository.save(enrollment));
    }

    @Transactional(readOnly = true)
    public List<Long> getTodayQuestions(String userEmail) {
        CourseEnrollment enrollment = enrollmentRepository.findByUserEmailAndEndedAtIsNull(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("활성 코스가 없습니다. email=" + userEmail));

        CoursePolicy policy = policyMap().get(enrollment.getCourseType());
        if (policy == null) {
            return List.of();
        }
        return policy.recommendQuestionIds(userEmail, 5);
    }
}
