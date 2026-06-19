package maeilmail.learning.domain.grading;

import java.util.List;

/**
 * 모범답안의 핵심 개념 하나.
 *
 * @param label    개념 이름 (피드백·약점에 노출)
 * @param keywords 이 개념을 짚었다고 인정할 표현들 (하나라도 답안에 있으면 매칭)
 * @param why      이 개념이 왜 중요한지 / 놓쳤을 때 보여줄 설명
 */
public record Concept(String label, List<String> keywords, String why) {
}
