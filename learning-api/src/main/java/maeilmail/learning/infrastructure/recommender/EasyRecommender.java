package maeilmail.learning.infrastructure.recommender;

import java.util.List;
import java.util.stream.LongStream;
import maeilmail.learning.domain.userstat.Difficulty;
import org.springframework.stereotype.Component;

@Component
public class EasyRecommender implements QuestionRecommender {

    @Override
    public Difficulty difficulty() {
        return Difficulty.EASY;
    }

    @Override
    public List<Long> recommend(String userEmail, int limit) {
        // EASY 문제: ID 1~50 범위 (실제 구현에서는 LegacyQuestionPort로 조회)
        return LongStream.rangeClosed(1, 50).limit(limit).boxed().toList();
    }
}
