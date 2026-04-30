package maeilmail.learning.infrastructure.recommender;

import java.util.List;
import java.util.stream.LongStream;
import maeilmail.learning.domain.userstat.Difficulty;
import org.springframework.stereotype.Component;

@Component
public class HardRecommender implements QuestionRecommender {

    @Override
    public Difficulty difficulty() {
        return Difficulty.HARD;
    }

    @Override
    public List<Long> recommend(String userEmail, int limit) {
        // HARD 문제: ID 101~200 범위
        return LongStream.rangeClosed(101, 200).limit(limit).boxed().toList();
    }
}
