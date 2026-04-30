package maeilmail.learning.domain.answer;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

@DataJpaTest
class AnswerRepositoryTest {

    @Autowired
    private AnswerRepository repository;

    @Test
    void 답안_저장_및_조회() {
        Answer answer = new Answer("user@t.com", 1L, "답변 내용", true, 90, 1200L);
        Answer saved = repository.save(answer);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getUserEmail()).isEqualTo("user@t.com");
        assertThat(saved.isCorrect()).isTrue();
    }

    @Test
    void findTop20_최신순_20개_반환() {
        for (int i = 1; i <= 25; i++) {
            repository.save(new Answer("user@t.com", (long) i, "답" + i, i % 2 == 0, 50 * i, 1000L));
        }

        List<Answer> top20 = repository.findTop20ByUserEmailOrderByCreatedAtDesc("user@t.com");

        assertThat(top20).hasSize(20);
    }

    @Test
    void findTop20_다른_사용자_결과_포함_안함() {
        repository.save(new Answer("a@t.com", 1L, "답", true, 80, 1000L));
        repository.save(new Answer("b@t.com", 2L, "답", false, 0, 2000L));

        List<Answer> result = repository.findTop20ByUserEmailOrderByCreatedAtDesc("a@t.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getUserEmail()).isEqualTo("a@t.com");
    }

    @Test
    void findTop20_데이터_없으면_빈_리스트() {
        List<Answer> result = repository.findTop20ByUserEmailOrderByCreatedAtDesc("none@t.com");

        assertThat(result).isEmpty();
    }
}
