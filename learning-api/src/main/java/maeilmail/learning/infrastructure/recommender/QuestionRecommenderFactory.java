package maeilmail.learning.infrastructure.recommender;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import maeilmail.learning.domain.userstat.Difficulty;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionRecommenderFactory {

    private final List<QuestionRecommender> recommenders;

    private Map<Difficulty, QuestionRecommender> recommenderMap() {
        return recommenders.stream()
                .collect(Collectors.toMap(QuestionRecommender::difficulty, Function.identity()));
    }

    public QuestionRecommender create(Difficulty difficulty) {
        QuestionRecommender recommender = recommenderMap().get(difficulty);
        if (recommender == null) {
            // 매핑 누락 시 EASY 반환 (안전 기본값)
            return recommenderMap().get(Difficulty.EASY);
        }
        return recommender;
    }
}
