package maeilmail.learning.infrastructure.recommender;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import maeilmail.learning.domain.userstat.Difficulty;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class QuestionRecommenderFactoryTest {

    private QuestionRecommenderFactory factory;

    @BeforeEach
    void setUp() {
        factory = new QuestionRecommenderFactory(List.of(
                new EasyRecommender(),
                new MediumRecommender(),
                new HardRecommender()
        ));
        factory.init();
    }

    @Test
    void EASY_난이도_추천기_생성() {
        QuestionRecommender recommender = factory.create(Difficulty.EASY);
        assertThat(recommender.difficulty()).isEqualTo(Difficulty.EASY);
        List<Long> ids = recommender.recommend("user@test.com", 5);
        assertThat(ids).hasSize(5);
        assertThat(ids).allMatch(id -> id >= 1 && id <= 50);
    }

    @Test
    void MEDIUM_난이도_추천기_생성() {
        QuestionRecommender recommender = factory.create(Difficulty.MEDIUM);
        assertThat(recommender.difficulty()).isEqualTo(Difficulty.MEDIUM);
        List<Long> ids = recommender.recommend("user@test.com", 3);
        assertThat(ids).allMatch(id -> id >= 51 && id <= 100);
    }

    @Test
    void HARD_난이도_추천기_생성() {
        QuestionRecommender recommender = factory.create(Difficulty.HARD);
        assertThat(recommender.difficulty()).isEqualTo(Difficulty.HARD);
        List<Long> ids = recommender.recommend("user@test.com", 3);
        assertThat(ids).allMatch(id -> id >= 101 && id <= 150);
    }

    @Test
    void 각_추천기는_서로_다른_문제_범위_반환() {
        List<Long> easy = factory.create(Difficulty.EASY).recommend("u", 5);
        List<Long> hard = factory.create(Difficulty.HARD).recommend("u", 5);
        assertThat(easy).doesNotContainAnyElementsOf(hard);
    }

    @Test
    void 지원하지_않는_난이도_예외_발생() {
        assertThatThrownBy(() -> factory.create(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("지원하지 않는 난이도");
    }
}
