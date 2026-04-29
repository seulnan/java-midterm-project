package maeilmail.learning.domain.course.policy;

import java.util.List;
import java.util.stream.LongStream;
import lombok.RequiredArgsConstructor;
import maeilmail.learning.domain.course.CourseType;
import maeilmail.learning.domain.userstat.Difficulty;
import maeilmail.learning.domain.userstat.UserStatRepository;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class HardOnlyPolicy implements CoursePolicy {

    private final UserStatRepository userStatRepository;

    @Override
    public CourseType courseType() {
        return CourseType.HARD_ONLY;
    }

    @Override
    public List<Long> recommendQuestionIds(String userEmail, int limit) {
        // 사용자가 HARD 레벨이 아니면 빈 목록 반환 (운영에서는 HARD 문제 풀에서 선택)
        boolean isHard = userStatRepository.findByUserEmail(userEmail)
                .map(stat -> stat.getCurrentDifficulty() == Difficulty.HARD)
                .orElse(false);

        if (!isHard) {
            return List.of();
        }

        // 단순화: HARD 문제 ID를 101~200 범위로 가정
        return LongStream.rangeClosed(101, 200)
                .limit(limit)
                .boxed()
                .toList();
    }
}
