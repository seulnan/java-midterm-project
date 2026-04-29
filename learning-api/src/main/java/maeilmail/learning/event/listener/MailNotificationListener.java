package maeilmail.learning.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.learning.domain.answer.event.AnswerSubmittedEvent;
import maeilmail.learning.infrastructure.mail.LearningMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailNotificationListener {

    private final LearningMailSender mailSender;

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AnswerSubmittedEvent event) {
        if (!event.isCorrect()) {
            log.info("[메일 큐] 오답 알림 발송: user={}, question={}", event.userEmail(), event.questionId());
            mailSender.send(
                    event.userEmail(),
                    "[매일메일 Plus] 오답 문제가 오답노트에 추가되었습니다",
                    String.format("questionId=%d 를 오답노트에서 복습하세요.", event.questionId())
            );
        }
    }
}
