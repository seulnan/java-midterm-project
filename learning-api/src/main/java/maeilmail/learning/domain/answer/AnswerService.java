package maeilmail.learning.domain.answer;

import lombok.RequiredArgsConstructor;
import maeilmail.learning.domain.answer.dto.SubmitAnswerRequest;
import maeilmail.learning.domain.answer.dto.SubmitAnswerResponse;
import maeilmail.learning.domain.answer.event.AnswerSubmittedEvent;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request) {
        // 단순화된 채점: 빈 텍스트가 아니면 정답으로 간주 (adapter slug에서 실제 채점으로 교체)
        boolean isCorrect = !request.submittedText().isBlank();
        int score = isCorrect ? 100 : 0;

        Answer answer = new Answer(
                request.userEmail(),
                request.questionId(),
                request.submittedText(),
                isCorrect,
                score,
                request.responseTimeMs()
        );
        Answer saved = answerRepository.save(answer);

        eventPublisher.publishEvent(new AnswerSubmittedEvent(
                saved.getId(),
                saved.getUserEmail(),
                saved.getQuestionId(),
                saved.isCorrect(),
                saved.getResponseTimeMs()
        ));

        return new SubmitAnswerResponse(
                saved.getId(),
                saved.isCorrect(),
                saved.getScore(),
                isCorrect ? "정답입니다!" : "오답입니다. 오답노트에 추가됩니다."
        );
    }
}
