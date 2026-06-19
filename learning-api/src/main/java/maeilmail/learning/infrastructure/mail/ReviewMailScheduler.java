package maeilmail.learning.infrastructure.mail;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.learning.adapter.LegacyQuestion;
import maeilmail.learning.adapter.LegacyQuestionPort;
import maeilmail.learning.domain.wrongnote.WrongNote;
import maeilmail.learning.domain.wrongnote.WrongNoteRepository;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReviewMailScheduler {

    private final WrongNoteRepository wrongNoteRepository;
    private final LegacyQuestionPort questionPort;
    private final LearningMailSender mailSender;

    // 매일 06:00 KST — 복습 기한이 도래한 문제가 있는 사용자에게 그 문제들을 모아 알림
    @Async("mailExecutor")
    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    public void sendReviewReminders() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        List<WrongNote> dueNotes = wrongNoteRepository.findAllByNextReviewAtLessThanEqual(now);

        Map<String, List<Long>> questionIdsByUser = dueNotes.stream()
                .collect(Collectors.groupingBy(
                        WrongNote::getUserEmail,
                        Collectors.mapping(WrongNote::getQuestionId, Collectors.toList())));

        questionIdsByUser.forEach((email, questionIds) -> {
            List<LegacyQuestion> dueQuestions = questionPort.findByIds(questionIds);
            QbitMailTemplate.Mail mail = QbitMailTemplate.reviewReminder(dueQuestions, email);
            log.info("[복습 메일] 발송: user={}, 문제수={}", email, dueQuestions.size());
            mailSender.send(email, mail.subject(), mail.html());
        });
    }
}
