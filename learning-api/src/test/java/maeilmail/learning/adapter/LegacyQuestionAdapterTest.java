package maeilmail.learning.adapter;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class LegacyQuestionAdapterTest {

    private LegacyQuestionAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new LegacyQuestionAdapter();
    }

    @Test
    void ID로_질문_조회() {
        Optional<LegacyQuestion> result = adapter.findById(1L);
        assertThat(result).isPresent();
        assertThat(result.get().id()).isEqualTo(1L);
        assertThat(result.get().title()).isEqualTo("Question #1");
    }

    @Test
    void 존재하지_않는_ID_조회시_빈_Optional_반환() {
        Optional<LegacyQuestion> result = adapter.findById(999L);
        assertThat(result).isEmpty();
    }

    @Test
    void 여러_ID로_질문_일괄_조회() {
        List<LegacyQuestion> questions = adapter.findByIds(List.of(1L, 2L, 3L));
        assertThat(questions).hasSize(3);
        assertThat(questions).extracting(LegacyQuestion::id).containsExactlyInAnyOrder(1L, 2L, 3L);
    }

    @Test
    void 혼합_ID_조회시_존재하는것만_반환() {
        List<LegacyQuestion> result = adapter.findByIds(List.of(1L, 999L, 2L));
        assertThat(result).hasSize(2);
        assertThat(result).extracting(LegacyQuestion::id).containsExactlyInAnyOrder(1L, 2L);
    }

    @Test
    void 카테고리로_질문_조회() {
        List<LegacyQuestion> backends = adapter.findByCategory("BACKEND");
        assertThat(backends).isNotEmpty();
        assertThat(backends).allMatch(q -> q.category().equalsIgnoreCase("BACKEND"));
    }

    @Test
    void 존재하지_않는_카테고리_조회시_빈_리스트_반환() {
        List<LegacyQuestion> result = adapter.findByCategory("UNKNOWN");
        assertThat(result).isEmpty();
    }

    @Test
    void 총_200개_문제_포함() {
        List<LegacyQuestion> all = adapter.findByIds(
                java.util.stream.LongStream.rangeClosed(1, 200).boxed().toList()
        );
        assertThat(all).hasSize(200);
    }
}
