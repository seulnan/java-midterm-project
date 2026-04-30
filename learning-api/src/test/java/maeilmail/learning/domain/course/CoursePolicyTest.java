package maeilmail.learning.domain.course;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;

import java.util.List;
import java.util.Optional;
import maeilmail.learning.domain.answer.Answer;
import maeilmail.learning.domain.answer.AnswerRepository;
import maeilmail.learning.domain.course.policy.HardOnlyPolicy;
import maeilmail.learning.domain.course.policy.ShortIntensivePolicy;
import maeilmail.learning.domain.course.policy.WeaknessFocusedPolicy;
import maeilmail.learning.domain.userstat.Difficulty;
import maeilmail.learning.domain.userstat.UserStat;
import maeilmail.learning.domain.userstat.UserStatRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CoursePolicyTest {

    @Mock private AnswerRepository answerRepository;
    @Mock private UserStatRepository userStatRepository;

    @InjectMocks private ShortIntensivePolicy shortIntensivePolicy;
    @InjectMocks private HardOnlyPolicy hardOnlyPolicy;
    @InjectMocks private WeaknessFocusedPolicy weaknessFocusedPolicy;

    @Test
    void SHORT_INTENSIVE_미시도_문제_5개_추천() {
        given(answerRepository.findTop20ByUserEmailOrderByCreatedAtDesc("user@test.com"))
                .willReturn(List.of());

        List<Long> ids = shortIntensivePolicy.recommendQuestionIds("user@test.com", 5);

        assertThat(ids).hasSize(5);
        assertThat(ids).doesNotHaveDuplicates();
    }

    @Test
    void SHORT_INTENSIVE_시도한_문제_제외하고_추천() {
        Answer attempted = new Answer("user@test.com", 1L, "답", true, 90, 1000L);
        given(answerRepository.findTop20ByUserEmailOrderByCreatedAtDesc("user@test.com"))
                .willReturn(List.of(attempted));

        List<Long> ids = shortIntensivePolicy.recommendQuestionIds("user@test.com", 3);

        assertThat(ids).doesNotContain(1L);
        assertThat(ids).hasSize(3);
    }

    @Test
    void HARD_ONLY_HARD_레벨_아닌_사용자는_빈_목록() {
        UserStat stat = UserStat.create("user@test.com");
        given(userStatRepository.findByUserEmail("user@test.com")).willReturn(Optional.of(stat));

        List<Long> ids = hardOnlyPolicy.recommendQuestionIds("user@test.com", 5);

        assertThat(ids).isEmpty();
    }

    @Test
    void HARD_ONLY_HARD_레벨_사용자는_문제_추천() {
        UserStat stat = UserStat.createHard("user@test.com");
        given(userStatRepository.findByUserEmail("user@test.com")).willReturn(Optional.of(stat));

        List<Long> ids = hardOnlyPolicy.recommendQuestionIds("user@test.com", 5);

        assertThat(ids).hasSize(5);
        assertThat(ids).allMatch(id -> id >= 101 && id <= 150);
    }

    @Test
    void WEAKNESS_FOCUSED_오답_문제_우선_추천() {
        Answer wrong = new Answer("user@test.com", 42L, "틀린답", false, 0, 1000L);
        given(answerRepository.findTop20ByUserEmailOrderByCreatedAtDesc("user@test.com"))
                .willReturn(List.of(wrong));

        List<Long> ids = weaknessFocusedPolicy.recommendQuestionIds("user@test.com", 5);

        assertThat(ids).contains(42L);
    }

    @Test
    void WEAKNESS_FOCUSED_오답_없으면_기본_문제_반환() {
        given(answerRepository.findTop20ByUserEmailOrderByCreatedAtDesc("user@test.com"))
                .willReturn(List.of());

        List<Long> ids = weaknessFocusedPolicy.recommendQuestionIds("user@test.com", 5);

        assertThat(ids).hasSize(5);
        assertThat(ids).allMatch(id -> id >= 1 && id <= 50);
    }

    @Test
    void 각_정책은_올바른_CourseType_반환() {
        assertThat(shortIntensivePolicy.courseType()).isEqualTo(CourseType.SHORT_INTENSIVE);
        assertThat(hardOnlyPolicy.courseType()).isEqualTo(CourseType.HARD_ONLY);
        assertThat(weaknessFocusedPolicy.courseType()).isEqualTo(CourseType.WEAKNESS_FOCUSED);
    }
}
