package maeilmail.learning.event;

import static org.mockito.Mockito.verify;

import java.util.List;
import maeilmail.learning.domain.answer.event.AnswerSubmittedEvent;
import maeilmail.learning.domain.grading.Concept;
import maeilmail.learning.domain.grading.GradeResult;
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
    void 오답_이벤트_수신_시_놓친_개념을_약점으로_오답노트_등록() {
        GradeResult grade = new GradeResult(33, false, List.of("기본 정의"),
                List.of(new Concept("동기화 필요성", List.of("동기화"), "공유 자원 접근 시 동기화가 필요합니다.")),
                "모범답안 요지");
        AnswerSubmittedEvent event = new AnswerSubmittedEvent(1L, "user@test.com", 10L, false, 1000L, grade);
        listener.handle(event);
        // 놓친 개념 라벨이 약점(weakness)으로 함께 전달돼야 한다.
        verify(wrongNoteService).registerOrSkip("user@test.com", 10L, "동기화 필요성");
    }

    @Test
    void 정답_이벤트_수신_시_오답노트_제거() {
        AnswerSubmittedEvent event = new AnswerSubmittedEvent(2L, "user@test.com", 10L, true, 800L);
        listener.handle(event);
        verify(wrongNoteService).removeIfExists("user@test.com", 10L);
    }
}
