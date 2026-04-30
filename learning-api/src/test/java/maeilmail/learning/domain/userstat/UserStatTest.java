package maeilmail.learning.domain.userstat;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class UserStatTest {

    @Test
    void 초기_생성_시_기본값_검증() {
        UserStat stat = UserStat.create("user@test.com");

        assertThat(stat.getUserEmail()).isEqualTo("user@test.com");
        assertThat(stat.getTotalAttempts()).isZero();
        assertThat(stat.getCorrectCount()).isZero();
        assertThat(stat.getCurrentDifficulty()).isEqualTo(Difficulty.EASY);
        assertThat(stat.getAvgResponseTimeMs()).isZero();
        assertThat(stat.getLastActiveAt()).isNull();
    }

    @Test
    void 정답_기록_시_totalAttempts와_correctCount_증가() {
        UserStat stat = UserStat.create("user@test.com");

        stat.recordAnswer(true, 1000L);

        assertThat(stat.getTotalAttempts()).isEqualTo(1);
        assertThat(stat.getCorrectCount()).isEqualTo(1);
        assertThat(stat.getLastActiveAt()).isNotNull();
    }

    @Test
    void 오답_기록_시_totalAttempts만_증가() {
        UserStat stat = UserStat.create("user@test.com");

        stat.recordAnswer(false, 1000L);

        assertThat(stat.getTotalAttempts()).isEqualTo(1);
        assertThat(stat.getCorrectCount()).isZero();
    }

    @Test
    void 응답시간_지수이동평균으로_갱신() {
        UserStat stat = UserStat.create("user@test.com");

        stat.recordAnswer(true, 1000L);
        long first = stat.getAvgResponseTimeMs();

        stat.recordAnswer(true, 5000L);
        long second = stat.getAvgResponseTimeMs();

        // 두 번째 기록 후 응답 시간은 첫 번째보다 커야 한다 (이동 평균)
        assertThat(second).isGreaterThan(first);
    }

    @Test
    void 문제수_20미만에서는_난이도_조정_없음() {
        UserStat stat = UserStat.create("user@test.com");

        // 19번 모두 정답 — 조정 불가
        for (int i = 0; i < 19; i++) {
            stat.recordAnswer(true, 500L);
        }

        assertThat(stat.getCurrentDifficulty()).isEqualTo(Difficulty.EASY);
    }

    @Test
    void 정답률_80초과_시_난이도_상승() {
        UserStat stat = UserStat.create("user@test.com");

        // 20번 모두 정답 (100%) → EASY → MEDIUM
        for (int i = 0; i < 20; i++) {
            stat.recordAnswer(true, 500L);
        }

        assertThat(stat.getCurrentDifficulty()).isEqualTo(Difficulty.MEDIUM);
    }

    @Test
    void 정답률_40미만_시_난이도_하강() {
        UserStat stat = UserStat.createHard("user@test.com");

        // 20번 모두 오답 (0%) → HARD → MEDIUM
        for (int i = 0; i < 20; i++) {
            stat.recordAnswer(false, 500L);
        }

        assertThat(stat.getCurrentDifficulty()).isEqualTo(Difficulty.MEDIUM);
    }

    @Test
    void EASY에서_하강_시도해도_EASY_유지() {
        UserStat stat = UserStat.create("user@test.com");

        for (int i = 0; i < 20; i++) {
            stat.recordAnswer(false, 500L);
        }

        assertThat(stat.getCurrentDifficulty()).isEqualTo(Difficulty.EASY);
    }

    @Test
    void HARD에서_상승_시도해도_HARD_유지() {
        UserStat stat = UserStat.createHard("user@test.com");

        for (int i = 0; i < 20; i++) {
            stat.recordAnswer(true, 500L);
        }

        assertThat(stat.getCurrentDifficulty()).isEqualTo(Difficulty.HARD);
    }

    @ParameterizedTest(name = "5의 배수 {0}문제에서만 난이도 평가")
    @CsvSource({"20,true", "25,true", "21,false", "22,false", "30,true"})
    void 배수5_시도에서만_난이도_평가(int attempts, boolean shouldAdjust) {
        UserStat stat = UserStat.create("user@test.com");

        // 전부 정답으로 채움
        for (int i = 0; i < attempts; i++) {
            stat.recordAnswer(true, 500L);
        }

        if (shouldAdjust) {
            assertThat(stat.getCurrentDifficulty()).isNotEqualTo(Difficulty.EASY);
        }
    }
}
