package maeilmail.learning.event.listener;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import java.util.List;
import maeilmail.learning.adapter.LegacyQuestion;
import maeilmail.learning.adapter.LegacyQuestionPort;
import maeilmail.learning.domain.answer.event.AnswerSubmittedEvent;
import maeilmail.learning.domain.grading.GradeResult;
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
        // 답안에 대한 피드백 메일을 보낸다 — 정답이면 칭찬, 오답이면 오답노트 + 복습 일정.
        // 문제 내용은 실제 데이터를 담는다(어댑터에 없으면 안전한 기본 문구로 대체).
        LegacyQuestion question = questionPort.findById(event.questionId())
                .orElseGet(() -> new LegacyQuestion(
                        event.questionId(), "제출한 문제", "오답노트에서 복습하세요.", "GENERAL"));

        GradeResult grade = event.grade() != null ? event.grade()
                : new GradeResult(event.isCorrect() ? 100 : 0, event.isCorrect(), List.of(), List.of(), null);

        QbitMailTemplate.Mail mail = QbitMailTemplate.answerFeedback(question, grade, event.userEmail());

        log.info("[피드백 메일] {} 발송: user={}, question={}, 점수={}",
                grade.correct() ? "정답" : "오답", event.userEmail(), event.questionId(), grade.score());
        mailSender.send(event.userEmail(), mail.subject(), mail.html());
    }
}
