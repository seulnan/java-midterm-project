package maeilmail.learning.domain.course;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@DataJpaTest
@EnableJpaAuditing
class CourseEnrollmentRepositoryTest {

    @Autowired
    private CourseEnrollmentRepository repository;

    @Test
    void 활성_코스_수강_저장_및_조회() {
        repository.save(CourseEnrollment.create("user@t.com", CourseType.SHORT_INTENSIVE));

        Optional<CourseEnrollment> found = repository.findByUserEmailAndEndedAtIsNull("user@t.com");

        assertThat(found).isPresent();
        assertThat(found.get().getCourseType()).isEqualTo(CourseType.SHORT_INTENSIVE);
        assertThat(found.get().isActive()).isTrue();
    }

    @Test
    void end_호출_후_활성_코스_없음() {
        CourseEnrollment enrollment = repository.save(
                CourseEnrollment.create("user@t.com", CourseType.HARD_ONLY));
        enrollment.end();
        repository.saveAndFlush(enrollment);

        Optional<CourseEnrollment> found = repository.findByUserEmailAndEndedAtIsNull("user@t.com");

        assertThat(found).isEmpty();
    }

    @Test
    void 종료_후_새_코스_등록_시_새것만_활성() {
        CourseEnrollment old = repository.saveAndFlush(
                CourseEnrollment.create("user@t.com", CourseType.WEAKNESS_FOCUSED));
        old.end();
        repository.saveAndFlush(old);
        repository.save(CourseEnrollment.create("user@t.com", CourseType.SHORT_INTENSIVE));

        Optional<CourseEnrollment> active = repository.findByUserEmailAndEndedAtIsNull("user@t.com");

        assertThat(active).isPresent();
        assertThat(active.get().getCourseType()).isEqualTo(CourseType.SHORT_INTENSIVE);
    }

    @Test
    void 수강_이력_없으면_빈_Optional() {
        Optional<CourseEnrollment> found = repository.findByUserEmailAndEndedAtIsNull("ghost@t.com");

        assertThat(found).isEmpty();
    }

    @Test
    void CourseEnrollment_startedAt_자동_설정() {
        CourseEnrollment enrollment = repository.saveAndFlush(
                CourseEnrollment.create("user@t.com", CourseType.HARD_ONLY));

        assertThat(enrollment.getStartedAt()).isNotNull();
    }
}
