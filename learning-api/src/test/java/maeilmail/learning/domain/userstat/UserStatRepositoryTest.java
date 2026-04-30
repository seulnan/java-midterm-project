package maeilmail.learning.domain.userstat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@DataJpaTest
@EnableJpaAuditing
class UserStatRepositoryTest {

    @Autowired
    private UserStatRepository repository;

    @Test
    void 사용자_통계_저장_및_이메일_조회() {
        repository.save(UserStat.create("user@t.com"));

        Optional<UserStat> found = repository.findByUserEmail("user@t.com");

        assertThat(found).isPresent();
        assertThat(found.get().getCurrentDifficulty()).isEqualTo(Difficulty.EASY);
        assertThat(found.get().getTotalAttempts()).isZero();
    }

    @Test
    void 동일_이메일_중복_저장_시_UniqueConstraint_위반() {
        repository.saveAndFlush(UserStat.create("dup@t.com"));

        assertThatThrownBy(() -> repository.saveAndFlush(UserStat.create("dup@t.com")))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 존재하지_않는_이메일_조회_시_빈_Optional() {
        Optional<UserStat> found = repository.findByUserEmail("ghost@t.com");

        assertThat(found).isEmpty();
    }

    @Test
    void version_필드_초기값_0() {
        UserStat stat = repository.saveAndFlush(UserStat.create("ver@t.com"));

        assertThat(stat.getVersion()).isEqualTo(0L);
    }

    @Test
    void recordAnswer_후_버전_증가() {
        UserStat stat = repository.saveAndFlush(UserStat.create("ver2@t.com"));
        stat.recordAnswer(true, 1000L);
        UserStat updated = repository.saveAndFlush(stat);

        assertThat(updated.getVersion()).isGreaterThan(0L);
    }

    @Test
    void findByUserEmailForUpdate_데이터_반환() {
        repository.saveAndFlush(UserStat.create("lock@t.com"));

        Optional<UserStat> result = repository.findByUserEmailForUpdate("lock@t.com");

        assertThat(result).isPresent();
    }
}
