package maeilmail.learning.infrastructure.recommender;

import jakarta.annotation.PostConstruct;
import java.util.Collections;
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
    private Map<Difficulty, QuestionRecommender> recommenderMap;

    @PostConstruct
    void init() {
        recommenderMap = Collections.unmodifiableMap(
                recommenders.stream()
                        .collect(Collectors.toMap(QuestionRecommender::difficulty, Function.identity()))
        );
    }

    public QuestionRecommender create(Difficulty difficulty) {
        QuestionRecommender recommender = recommenderMap.get(difficulty);
        if (recommender == null) {
            throw new IllegalArgumentException("지원하지 않는 난이도입니다: " + difficulty);
        }
        return recommender;
    }
}
