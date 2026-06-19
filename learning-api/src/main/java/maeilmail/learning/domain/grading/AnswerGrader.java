package maeilmail.learning.domain.grading;

import java.util.ArrayList;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * 개념-커버리지 채점기.
 *
 * <p>제출 답안을 정규화(소문자·공백 제거)한 뒤, 질문의 핵심 개념마다 키워드가 하나라도
 * 등장하는지로 '짚었다/놓쳤다'를 판정한다. 점수는 짚은 개념 비율, 통과는 60점 이상.
 * 놓친 개념이 곧 약점이며, 각 개념의 why가 "왜 오답인지" 피드백의 근거가 된다.
 *
 * <p>외부 의존이 없는 결정적(deterministic) 알고리즘이라 단위 테스트로 검증 가능하다.
 */
@Component
public class AnswerGrader {

    private static final int PASS_SCORE = 60;

    public GradeResult grade(Long questionId, String submission) {
        QuestionKey key = GradingKeys.get(questionId);
        String normalized = normalize(submission);

        if (key == null) {
            // 채점 기준이 등록되지 않은 질문 — 보수적으로 미통과 처리(빈 답은 0점).
            int score = normalized.isEmpty() ? 0 : 50;
            return new GradeResult(score, false, List.of(), List.of(), null);
        }

        List<String> matched = new ArrayList<>();
        List<Concept> missed = new ArrayList<>();
        for (Concept concept : key.concepts()) {
            if (hits(concept, normalized)) {
                matched.add(concept.label());
            } else {
                missed.add(concept);
            }
        }

        int total = key.concepts().size();
        int score = total == 0 ? 0 : (int) Math.round(matched.size() * 100.0 / total);
        boolean correct = score >= PASS_SCORE;
        return new GradeResult(score, correct, matched, missed, key.modelAnswer());
    }

    private boolean hits(Concept concept, String normalizedSubmission) {
        return concept.keywords().stream()
                .anyMatch(keyword -> normalizedSubmission.contains(normalize(keyword)));
    }

    private static String normalize(String text) {
        if (text == null) {
            return "";
        }
        return text.toLowerCase().replaceAll("\\s+", "");
    }
}
