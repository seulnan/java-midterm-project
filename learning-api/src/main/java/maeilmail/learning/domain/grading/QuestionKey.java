package maeilmail.learning.domain.grading;

import java.util.List;

/** 한 질문의 채점 기준 — 모범답안 요지 + 핵심 개념 목록. */
public record QuestionKey(String modelAnswer, List<Concept> concepts) {
}
