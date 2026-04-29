package maeilmail.learning.domain.answer;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import maeilmail.learning.domain.answer.dto.SubmitAnswerRequest;
import maeilmail.learning.domain.answer.dto.SubmitAnswerResponse;
import maeilmail.learning.domain.answer.event.AnswerSubmittedEvent;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

@ExtendWith(MockitoExtension.class)
class AnswerServiceTest {

    @Mock
    private AnswerRepository answerRepository;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @InjectMocks
    private AnswerService answerService;

    @Test
    void 정답_제출_시_isCorrect_true_반환() {
        Answer saved = new Answer("test@test.com", 1L, "정답 내용", true, 100, 1500L);
        given(answerRepository.save(any())).willReturn(saved);

        SubmitAnswerRequest request = new SubmitAnswerRequest(1L, "정답 내용", 1500L, "test@test.com");
        SubmitAnswerResponse response = answerService.submitAnswer(request);

        assertThat(response.isCorrect()).isTrue();
        assertThat(response.score()).isEqualTo(100);
    }

    @Test
    void 빈_텍스트_제출_시_오답_처리() {
        Answer saved = new Answer("test@test.com", 1L, " ", false, 0, 500L);
        given(answerRepository.save(any())).willReturn(saved);

        SubmitAnswerRequest request = new SubmitAnswerRequest(1L, " ", 500L, "test@test.com");
        SubmitAnswerResponse response = answerService.submitAnswer(request);

        assertThat(response.isCorrect()).isFalse();
        assertThat(response.score()).isEqualTo(0);
    }

    @Test
    void 답안_제출_시_이벤트_발행() {
        Answer saved = new Answer("user@test.com", 2L, "내용", true, 100, 2000L);
        given(answerRepository.save(any())).willReturn(saved);

        SubmitAnswerRequest request = new SubmitAnswerRequest(2L, "내용", 2000L, "user@test.com");
        answerService.submitAnswer(request);

        ArgumentCaptor<AnswerSubmittedEvent> captor = ArgumentCaptor.forClass(AnswerSubmittedEvent.class);
        verify(eventPublisher).publishEvent(captor.capture());
        AnswerSubmittedEvent event = captor.getValue();

        assertThat(event.userEmail()).isEqualTo("user@test.com");
        assertThat(event.questionId()).isEqualTo(2L);
        assertThat(event.isCorrect()).isTrue();
    }
}
