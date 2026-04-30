package maeilmail.learning.event;

import static org.mockito.Mockito.verify;

import maeilmail.learning.domain.answer.event.AnswerSubmittedEvent;
import maeilmail.learning.domain.userstat.UserStatService;
import maeilmail.learning.event.listener.UserStatUpdateListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserStatUpdateListenerTest {

    @Mock
    private UserStatService userStatService;

    @InjectMocks
    private UserStatUpdateListener listener;

    @Test
    void 정답_이벤트_수신_시_통계_갱신() {
        AnswerSubmittedEvent event = new AnswerSubmittedEvent(1L, "user@test.com", 10L, true, 1200L);
        listener.handle(event);
        verify(userStatService).recordAnswer("user@test.com", true, 1200L);
    }

    @Test
    void 오답_이벤트_수신_시_통계_갱신() {
        AnswerSubmittedEvent event = new AnswerSubmittedEvent(2L, "user@test.com", 20L, false, 3000L);
        listener.handle(event);
        verify(userStatService).recordAnswer("user@test.com", false, 3000L);
    }
}
