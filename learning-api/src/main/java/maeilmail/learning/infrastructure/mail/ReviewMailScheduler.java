package maeilmail.learning.infrastructure.mail;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
    private final LearningMailSender mailSender;

    // 매일 06:00 KST — 복습 대상이 있는 사용자에게 알림
    @Async("mailExecutor")
    @Scheduled(cron = "0 0 6 * * *", zone = "Asia/Seoul")
    @Transactional(readOnly = true)
    public void sendReviewReminders() {
        LocalDateTime now = LocalDateTime.now(ZoneId.of("Asia/Seoul"));
        List<WrongNote> dueNotes = wrongNoteRepository.findAllByNextReviewAtLessThanEqual(now);

        dueNotes.stream()
                .map(WrongNote::getUserEmail)
                .distinct()
                .forEach(email -> {
                    log.info("[복습 메일] 발송: user={}", email);
                    mailSender.send(
                            email,
                            "[매일메일 Plus] 오늘의 복습 문제가 있습니다",
                            "오답노트 복습 문제를 확인하세요: /api/wrong-notes/me/due?email=" + email
                    );
                });
    }
}
