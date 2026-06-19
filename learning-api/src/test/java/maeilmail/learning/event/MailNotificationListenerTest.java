package maeilmail.learning.event;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import maeilmail.learning.adapter.LegacyQuestion;
import maeilmail.learning.adapter.LegacyQuestionPort;
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

    @Mock
    private LegacyQuestionPort questionPort;

    @InjectMocks
    private MailNotificationListener listener;

    @Test
    void 오답_이벤트_수신_시_실제_질문을_담아_메일_발송() {
        given(questionPort.findById(10L)).willReturn(Optional.of(
                new LegacyQuestion(10L, "트랜잭션의 ACID 속성을 설명해주세요.", "원자성/일관성/격리성/지속성...", "BACKEND")));
        AnswerSubmittedEvent event = new AnswerSubmittedEvent(1L, "user@test.com", 10L, false, 1000L);

        listener.handle(event);

        // 제목에는 "오답", 본문(HTML)에는 실제 질문 제목이 담겨야 한다.
        verify(mailSender).send(eq("user@test.com"), contains("오답"), contains("ACID"));
    }

    @Test
    void 정답_이벤트_수신_시_메일_미발송_및_질문조회_안함() {
        AnswerSubmittedEvent event = new AnswerSubmittedEvent(2L, "user@test.com", 10L, true, 800L);

        listener.handle(event);

        verify(mailSender, never()).send(anyString(), anyString(), anyString());
        verify(questionPort, never()).findById(any());
    }
}
