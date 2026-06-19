package maeilmail.learning.domain.grading;

import java.util.List;

/**
 * 채점 결과.
 *
 * @param score       0~100 (짚은 핵심 개념 비율)
 * @param correct     통과 여부 (score >= 통과 임계값)
 * @param matched     짚은 개념 라벨
 * @param missed      놓친 개념(약점) — label + why 포함
 * @param modelAnswer 모범답안 요지 (채점 기준이 없으면 null)
 */
public record GradeResult(
        int score,
        boolean correct,
        List<String> matched,
        List<Concept> missed,
        String modelAnswer
) {
    /** 채점 기준이 등록된 질문이었는지. */
    public boolean graded() {
        return modelAnswer != null;
    }
}
