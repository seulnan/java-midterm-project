package maeilmail.learning.event.listener;

import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.learning.domain.answer.event.AnswerSubmittedEvent;
import maeilmail.learning.domain.grading.Concept;
import maeilmail.learning.domain.grading.GradeResult;
import maeilmail.learning.domain.wrongnote.WrongNoteService;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class WrongNoteRegistrationListener {

    private final WrongNoteService wrongNoteService;

    // 답안 저장 트랜잭션이 커밋된 후 실행 — 롤백 시 오답노트에 영향 없음
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AnswerSubmittedEvent event) {
        log.debug("오답노트 처리: user={}, question={}, correct={}", event.userEmail(), event.questionId(), event.isCorrect());
        if (event.isCorrect()) {
            wrongNoteService.removeIfExists(event.userEmail(), event.questionId());
        } else {
            // 채점에서 놓친 개념(약점)을 오답노트에 함께 기록한다 — "왜 오답인지".
            wrongNoteService.registerOrSkip(event.userEmail(), event.questionId(), weaknessOf(event.grade()));
        }
    }

    private String weaknessOf(GradeResult grade) {
        if (grade == null || grade.missed().isEmpty()) {
            return null;
        }
        return grade.missed().stream().map(Concept::label).collect(Collectors.joining(", "));
    }
}
