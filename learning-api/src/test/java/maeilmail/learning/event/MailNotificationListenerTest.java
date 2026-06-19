package maeilmail.learning.event;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
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
    void 오답_이벤트_수신_시_오답_피드백_메일_발송() {
        given(questionPort.findById(10L)).willReturn(Optional.of(
                new LegacyQuestion(10L, "트랜잭션의 ACID 속성을 설명해주세요.", "원자성/일관성/격리성/지속성...", "BACKEND")));
        AnswerSubmittedEvent event = new AnswerSubmittedEvent(1L, "user@test.com", 10L, false, 1000L);

        listener.handle(event);

        // 오답 → 제목에 "오답", 본문(HTML)에 실제 질문 제목.
        verify(mailSender).send(eq("user@test.com"), contains("오답"), contains("ACID"));
    }

    @Test
    void 정답_이벤트_수신_시_정답_피드백_메일_발송() {
        given(questionPort.findById(10L)).willReturn(Optional.of(
                new LegacyQuestion(10L, "트랜잭션의 ACID 속성을 설명해주세요.", "원자성/일관성/격리성/지속성...", "BACKEND")));
        AnswerSubmittedEvent event = new AnswerSubmittedEvent(2L, "user@test.com", 10L, true, 800L);

        listener.handle(event);

        // 정답 → 제목에 "정답", 본문에 실제 질문 제목. (이제 정답에도 피드백 메일을 보낸다)
        verify(mailSender).send(eq("user@test.com"), contains("정답"), contains("ACID"));
    }
}
