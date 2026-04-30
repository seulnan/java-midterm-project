package maeilmail.learning.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.learning.domain.answer.event.AnswerSubmittedEvent;
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
            wrongNoteService.registerOrSkip(event.userEmail(), event.questionId());
        }
    }
}
