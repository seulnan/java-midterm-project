package maeilmail.learning.domain.course.policy;

import java.util.List;
import java.util.stream.LongStream;
import lombok.RequiredArgsConstructor;
import maeilmail.learning.domain.answer.AnswerRepository;
import maeilmail.learning.domain.course.CourseType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ShortIntensivePolicy implements CoursePolicy {

    private final AnswerRepository answerRepository;

    @Override
    public CourseType courseType() {
        return CourseType.SHORT_INTENSIVE;
    }

    @Override
    public List<Long> recommendQuestionIds(String userEmail, int limit) {
        // 이미 시도한 문제 ID 제외하고 순차 추천 (단순화: 1~N 중 미시도 문제)
        List<Long> attempted = answerRepository.findTop20ByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .map(a -> a.getQuestionId())
                .toList();

        return LongStream.iterate(1, id -> id + 1)
                .filter(id -> !attempted.contains(id))
                .limit(limit)
                .boxed()
                .toList();
    }
}
