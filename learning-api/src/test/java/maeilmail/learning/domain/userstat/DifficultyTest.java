package maeilmail.learning.domain.userstat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class DifficultyTest {

    @Test
    void EASY_upgrade_returns_MEDIUM() {
        assertThat(Difficulty.EASY.upgrade()).isEqualTo(Difficulty.MEDIUM);
    }

    @Test
    void MEDIUM_upgrade_returns_HARD() {
        assertThat(Difficulty.MEDIUM.upgrade()).isEqualTo(Difficulty.HARD);
    }

    @Test
    void HARD_upgrade_returns_HARD_상한_고정() {
        assertThat(Difficulty.HARD.upgrade()).isEqualTo(Difficulty.HARD);
    }

    @Test
    void EASY_downgrade_returns_EASY_하한_고정() {
        assertThat(Difficulty.EASY.downgrade()).isEqualTo(Difficulty.EASY);
    }

    @Test
    void MEDIUM_downgrade_returns_EASY() {
        assertThat(Difficulty.MEDIUM.downgrade()).isEqualTo(Difficulty.EASY);
    }

    @Test
    void HARD_downgrade_returns_MEDIUM() {
        assertThat(Difficulty.HARD.downgrade()).isEqualTo(Difficulty.MEDIUM);
    }

    @Test
    void upgrade_downgrade_inverse_for_MEDIUM() {
        assertThat(Difficulty.MEDIUM.upgrade().downgrade()).isEqualTo(Difficulty.MEDIUM);
        assertThat(Difficulty.MEDIUM.downgrade().upgrade()).isEqualTo(Difficulty.MEDIUM);
    }
}
