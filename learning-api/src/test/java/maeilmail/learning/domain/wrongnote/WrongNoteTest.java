package maeilmail.learning.domain.wrongnote;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class WrongNoteTest {

    @Test
    void 초기_생성_시_기본값_확인() {
        WrongNote note = WrongNote.create("user@test.com", 1L);

        assertThat(note.getEaseFactor()).isEqualTo(2.5);
        assertThat(note.getIntervalDays()).isEqualTo(1);
        assertThat(note.getReviewCount()).isEqualTo(0);
    }

    @Test
    void 정답_복습_시_interval과_easeFactor_증가() {
        WrongNote note = WrongNote.create("user@test.com", 1L);
        double initialEase = note.getEaseFactor();
        int initialInterval = note.getIntervalDays();

        note.applyReview(true);

        assertThat(note.getEaseFactor()).isGreaterThan(initialEase);
        assertThat(note.getIntervalDays()).isGreaterThanOrEqualTo(initialInterval);
        assertThat(note.getReviewCount()).isEqualTo(1);
        assertThat(note.getLastReviewedAt()).isNotNull();
        assertThat(note.getNextReviewAt()).isAfter(note.getLastReviewedAt());
    }

    @Test
    void 오답_복습_시_interval_리셋_및_easeFactor_감소() {
        WrongNote note = WrongNote.create("user@test.com", 1L);
        note.applyReview(true);
        note.applyReview(true);
        double easeBeforeWrong = note.getEaseFactor();

        note.applyReview(false);

        assertThat(note.getIntervalDays()).isEqualTo(1);
        assertThat(note.getEaseFactor()).isLessThan(easeBeforeWrong);
    }

    @Test
    void easeFactor는_1_3_아래로_떨어지지_않는다() {
        WrongNote note = WrongNote.create("user@test.com", 1L);

        // easeFactor가 1.3에 수렴할 때까지 반복 오답
        for (int i = 0; i < 20; i++) {
            note.applyReview(false);
        }

        assertThat(note.getEaseFactor()).isGreaterThanOrEqualTo(1.3);
    }

    @Test
    void 연속_정답_시_interval_점진적_증가() {
        WrongNote note = WrongNote.create("user@test.com", 1L);

        int prev = note.getIntervalDays();
        for (int i = 0; i < 5; i++) {
            note.applyReview(true);
            assertThat(note.getIntervalDays()).isGreaterThanOrEqualTo(prev);
            prev = note.getIntervalDays();
        }
        // 5회 정답 후 interval은 초기값(1)보다 커야 함
        assertThat(note.getIntervalDays()).isGreaterThan(1);
    }
}
