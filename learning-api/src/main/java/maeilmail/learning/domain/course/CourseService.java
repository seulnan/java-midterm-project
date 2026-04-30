package maeilmail.learning.domain.course;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
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
    private Map<CourseType, CoursePolicy> policyMap;

    @PostConstruct
    void init() {
        policyMap = Collections.unmodifiableMap(
                policies.stream().collect(Collectors.toUnmodifiableMap(CoursePolicy::courseType, Function.identity()))
        );
    }

    @Transactional
    public CourseEnrollmentDto enroll(EnrollRequest request) {
        enrollmentRepository.findByUserEmailAndEndedAtIsNull(request.userEmail())
                .ifPresent(CourseEnrollment::end);

        CourseEnrollment enrollment = CourseEnrollment.create(request.userEmail(), request.courseType());
        return CourseEnrollmentDto.from(enrollmentRepository.save(enrollment));
    }

    @Transactional(readOnly = true)
    public List<Long> getTodayQuestions(String userEmail) {
        CourseEnrollment enrollment = enrollmentRepository.findByUserEmailAndEndedAtIsNull(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("활성 코스가 없습니다. email=" + userEmail));

        CoursePolicy policy = policyMap.get(enrollment.getCourseType());
        if (policy == null) {
            return List.of();
        }
        return policy.recommendQuestionIds(userEmail, 5);
    }
}
