package maeilmail.learning.domain.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import maeilmail.learning.common.exception.ResourceNotFoundException;
import maeilmail.learning.domain.answer.AnswerRepository;
import maeilmail.learning.domain.course.dto.CourseEnrollmentDto;
import maeilmail.learning.domain.course.dto.EnrollRequest;
import maeilmail.learning.domain.course.policy.HardOnlyPolicy;
import maeilmail.learning.domain.course.policy.ShortIntensivePolicy;
import maeilmail.learning.domain.course.policy.WeaknessFocusedPolicy;
import maeilmail.learning.domain.userstat.UserStatRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

    @Mock
    private CourseEnrollmentRepository enrollmentRepository;
    @Mock
    private AnswerRepository answerRepository;
    @Mock
    private UserStatRepository userStatRepository;

    private CourseService courseService;

    @BeforeEach
    void setUp() {
        List<maeilmail.learning.domain.course.policy.CoursePolicy> policies = List.of(
                new ShortIntensivePolicy(answerRepository),
                new HardOnlyPolicy(userStatRepository),
                new WeaknessFocusedPolicy(answerRepository)
        );
        courseService = new CourseService(enrollmentRepository, policies);
        courseService.init();
    }

    @Test
    void enroll_신규_코스_등록() {
        CourseEnrollment saved = CourseEnrollment.create("u@t.com", CourseType.SHORT_INTENSIVE);
        given(enrollmentRepository.findByUserEmailAndEndedAtIsNull("u@t.com"))
                .willReturn(Optional.empty());
        given(enrollmentRepository.save(any())).willReturn(saved);

        CourseEnrollmentDto dto = courseService.enroll(new EnrollRequest("u@t.com", CourseType.SHORT_INTENSIVE));

        assertThat(dto.courseType()).isEqualTo(CourseType.SHORT_INTENSIVE);
        assertThat(dto.userEmail()).isEqualTo("u@t.com");
        verify(enrollmentRepository).save(any(CourseEnrollment.class));
    }

    @Test
    void enroll_기존_활성_코스_종료_후_새_코스_등록() {
        CourseEnrollment existing = CourseEnrollment.create("u@t.com", CourseType.HARD_ONLY);
        CourseEnrollment newEnroll = CourseEnrollment.create("u@t.com", CourseType.SHORT_INTENSIVE);

        given(enrollmentRepository.findByUserEmailAndEndedAtIsNull("u@t.com"))
                .willReturn(Optional.of(existing));
        given(enrollmentRepository.save(any())).willReturn(newEnroll);

        courseService.enroll(new EnrollRequest("u@t.com", CourseType.SHORT_INTENSIVE));

        assertThat(existing.isActive()).isFalse();
        verify(enrollmentRepository).save(any());
    }

    @Test
    void getTodayQuestions_활성_코스_기반_문제_반환() {
        CourseEnrollment enrollment = CourseEnrollment.create("u@t.com", CourseType.SHORT_INTENSIVE);
        given(enrollmentRepository.findByUserEmailAndEndedAtIsNull("u@t.com"))
                .willReturn(Optional.of(enrollment));
        given(answerRepository.findTop20ByUserEmailOrderByCreatedAtDesc("u@t.com"))
                .willReturn(List.of());

        List<Long> ids = courseService.getTodayQuestions("u@t.com");

        assertThat(ids).hasSize(5);
    }

    @Test
    void getTodayQuestions_활성_코스_없으면_예외() {
        given(enrollmentRepository.findByUserEmailAndEndedAtIsNull("u@t.com"))
                .willReturn(Optional.empty());

        assertThatThrownBy(() -> courseService.getTodayQuestions("u@t.com"))
                .isInstanceOf(ResourceNotFoundException.class);
    }
}
