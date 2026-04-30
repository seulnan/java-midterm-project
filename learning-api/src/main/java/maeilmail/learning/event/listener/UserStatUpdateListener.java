package maeilmail.learning.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.learning.domain.answer.event.AnswerSubmittedEvent;
import maeilmail.learning.domain.userstat.UserStatService;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class UserStatUpdateListener {

    private final UserStatService userStatService;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AnswerSubmittedEvent event) {
        log.debug("통계 갱신: user={}, correct={}", event.userEmail(), event.isCorrect());
        userStatService.recordAnswer(event.userEmail(), event.isCorrect(), event.responseTimeMs());
    }
}
