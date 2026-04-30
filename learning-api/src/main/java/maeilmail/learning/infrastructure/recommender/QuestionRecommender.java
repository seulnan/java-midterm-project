package maeilmail.learning.infrastructure.recommender;

import java.util.List;
import maeilmail.learning.domain.userstat.Difficulty;

public interface QuestionRecommender {

    Difficulty difficulty();

    List<Long> recommend(String userEmail, int limit);
}
