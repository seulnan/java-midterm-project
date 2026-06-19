package maeilmail.learning.domain.grading;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class AnswerGraderTest {

    private final AnswerGrader grader = new AnswerGrader();

    @Test
    void 핵심_개념을_모두_짚으면_만점_통과() {
        // Q12: 메모리 공유 / 컨텍스트 스위칭 / 동기화 3개 모두 언급
        GradeResult r = grader.grade(12L,
                "스레드는 같은 프로세스의 힙 메모리를 공유하고 스택은 따로 가집니다. "
                        + "컨텍스트 스위칭 비용이 더 작고, 공유 자원 접근 시 동기화가 필요합니다.");

        assertThat(r.score()).isEqualTo(100);
        assertThat(r.correct()).isTrue();
        assertThat(r.missed()).isEmpty();
    }

    @Test
    void 일부만_짚으면_점수가_깎이고_놓친_개념이_약점으로_남는다() {
        // "작은 단위" 수준 — 어떤 핵심 키워드도 없음
        GradeResult r = grader.grade(12L, "프로세스는 프로그램이고 스레드는 그 안의 작은 단위입니다.");

        assertThat(r.score()).isLessThan(60);
        assertThat(r.correct()).isFalse();
        assertThat(r.missed()).extracting(Concept::label)
                .contains("메모리 공유 여부(힙 공유·스택 별도)", "컨텍스트 스위칭 비용", "동기화 필요성");
        // 약점마다 "왜"가 달려 있어야 한다
        assertThat(r.missed()).allSatisfy(c -> assertThat(c.why()).isNotBlank());
    }

    @Test
    void 빈_답안은_0점() {
        GradeResult r = grader.grade(12L, "   ");
        assertThat(r.score()).isZero();
        assertThat(r.correct()).isFalse();
        assertThat(r.matched()).isEmpty();
    }

    @Test
    void 채점기준_없는_질문은_graded_false() {
        GradeResult r = grader.grade(999L, "아무 답");
        assertThat(r.graded()).isFalse();
        assertThat(r.correct()).isFalse();
    }

    @Test
    void 통과_임계값_경계_3개중_2개면_통과() {
        // Q8: 멱등성 + 전체/부분 교체 2개 언급 (67점 → 통과)
        GradeResult r = grader.grade(8L, "PUT은 전체 교체라 멱등하고, PATCH는 부분 수정입니다.");
        assertThat(r.score()).isGreaterThanOrEqualTo(60);
        assertThat(r.correct()).isTrue();
    }
}
