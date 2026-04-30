package maeilmail.learning.infrastructure.recommender;

import java.util.List;
import java.util.stream.LongStream;
import maeilmail.learning.domain.userstat.Difficulty;
import org.springframework.stereotype.Component;

@Component
public class MediumRecommender implements QuestionRecommender {

    @Override
    public Difficulty difficulty() {
        return Difficulty.MEDIUM;
    }

    @Override
    public List<Long> recommend(String userEmail, int limit) {
        // MEDIUM 문제: ID 51~100 범위
        return LongStream.rangeClosed(51, 100).limit(limit).boxed().toList();
    }
}
