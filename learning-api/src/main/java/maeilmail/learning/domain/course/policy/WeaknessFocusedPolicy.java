package maeilmail.learning.domain.course.policy;

import java.util.List;
import java.util.stream.LongStream;
import lombok.RequiredArgsConstructor;
import maeilmail.learning.domain.answer.AnswerRepository;
import maeilmail.learning.domain.course.CourseType;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class WeaknessFocusedPolicy implements CoursePolicy {

    private final AnswerRepository answerRepository;

    @Override
    public CourseType courseType() {
        return CourseType.WEAKNESS_FOCUSED;
    }

    @Override
    public List<Long> recommendQuestionIds(String userEmail, int limit) {
        // 오답 문제 ID 우선 추천 (정답률 하위 = 틀린 문제)
        List<Long> wrongIds = answerRepository.findTop20ByUserEmailOrderByCreatedAtDesc(userEmail)
                .stream()
                .filter(a -> !a.isCorrect())
                .map(a -> a.getQuestionId())
                .distinct()
                .limit(limit)
                .toList();

        if (!wrongIds.isEmpty()) {
            return wrongIds;
        }

        // 오답 없으면 전체 중 랜덤 (단순화: 1~50)
        return LongStream.rangeClosed(1, 50)
                .limit(limit)
                .boxed()
                .toList();
    }
}
