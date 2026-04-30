package maeilmail.learning.event;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import maeilmail.learning.domain.answer.event.AnswerSubmittedEvent;
import maeilmail.learning.event.listener.MailNotificationListener;
import maeilmail.learning.infrastructure.mail.LearningMailSender;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MailNotificationListenerTest {

    @Mock
    private LearningMailSender mailSender;

    @InjectMocks
    private MailNotificationListener listener;

    @Test
    void 오답_이벤트_수신_시_메일_발송() {
        AnswerSubmittedEvent event = new AnswerSubmittedEvent(1L, "user@test.com", 10L, false, 1000L);
        listener.handle(event);
        verify(mailSender).send(eq("user@test.com"), contains("오답"), contains("10"));
    }

    @Test
    void 정답_이벤트_수신_시_메일_미발송() {
        AnswerSubmittedEvent event = new AnswerSubmittedEvent(2L, "user@test.com", 10L, true, 800L);
        listener.handle(event);
        verify(mailSender, never()).send(eq("user@test.com"), contains("오답"), contains("10"));
    }
}
