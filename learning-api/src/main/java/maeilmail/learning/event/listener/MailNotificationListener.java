package maeilmail.learning.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.learning.adapter.LegacyQuestion;
import maeilmail.learning.adapter.LegacyQuestionPort;
import maeilmail.learning.domain.answer.event.AnswerSubmittedEvent;
import maeilmail.learning.infrastructure.mail.LearningMailSender;
import maeilmail.learning.infrastructure.mail.QbitMailTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

@Slf4j
@Component
@RequiredArgsConstructor
public class MailNotificationListener {

    private final LearningMailSender mailSender;
    private final LegacyQuestionPort questionPort;

    @Async("mailExecutor")
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handle(AnswerSubmittedEvent event) {
        if (event.isCorrect()) {
            return;
        }
        // 방금 틀린 문제의 실제 내용을 메일에 담는다. (어댑터에 없으면 안전한 기본 문구로 대체)
        LegacyQuestion question = questionPort.findById(event.questionId())
                .orElseGet(() -> new LegacyQuestion(
                        event.questionId(), "오답 문제", "오답노트에서 복습하세요.", "GENERAL"));

        QbitMailTemplate.Mail mail = QbitMailTemplate.wrongAnswerNotice(question, event.userEmail());
        log.info("[메일 큐] 오답 알림 발송: user={}, question={}", event.userEmail(), event.questionId());
        mailSender.send(event.userEmail(), mail.subject(), mail.html());
    }
}
