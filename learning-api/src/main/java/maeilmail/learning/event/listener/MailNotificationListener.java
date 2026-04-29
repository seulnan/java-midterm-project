package maeilmail.learning.event.listener;

import lombok.extern.slf4j.Slf4j;
import maeilmail.learning.domain.answer.event.AnswerSubmittedEvent;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
public class MailNotificationListener {

    // 메일 발송 큐잉 — 실제 발송 로직은 mail-strategy slug에서 LearningMailSender 주입 후 연결
    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AnswerSubmittedEvent event) {
        if (!event.isCorrect()) {
            log.info("[메일 큐] 오답 알림 예약: user={}, question={}", event.userEmail(), event.questionId());
        }
    }
}
