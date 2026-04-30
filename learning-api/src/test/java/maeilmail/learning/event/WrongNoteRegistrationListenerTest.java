package maeilmail.learning.event;

import static org.mockito.Mockito.verify;

import maeilmail.learning.domain.answer.event.AnswerSubmittedEvent;
import maeilmail.learning.domain.wrongnote.WrongNoteService;
import maeilmail.learning.event.listener.WrongNoteRegistrationListener;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class WrongNoteRegistrationListenerTest {

    @Mock
    private WrongNoteService wrongNoteService;

    @InjectMocks
    private WrongNoteRegistrationListener listener;

    @Test
    void 오답_이벤트_수신_시_오답노트_등록() {
        AnswerSubmittedEvent event = new AnswerSubmittedEvent(1L, "user@test.com", 10L, false, 1000L);
        listener.handle(event);
        verify(wrongNoteService).registerOrSkip("user@test.com", 10L);
    }

    @Test
    void 정답_이벤트_수신_시_오답노트_제거() {
        AnswerSubmittedEvent event = new AnswerSubmittedEvent(2L, "user@test.com", 10L, true, 800L);
        listener.handle(event);
        verify(wrongNoteService).removeIfExists("user@test.com", 10L);
    }
}
