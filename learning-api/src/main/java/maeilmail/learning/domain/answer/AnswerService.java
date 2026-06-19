package maeilmail.learning.domain.answer;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.learning.domain.answer.dto.SubmitAnswerRequest;
import maeilmail.learning.domain.answer.dto.SubmitAnswerResponse;
import maeilmail.learning.domain.answer.event.AnswerSubmittedEvent;
import maeilmail.learning.domain.grading.AnswerGrader;
import maeilmail.learning.domain.grading.GradeResult;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final AnswerGrader answerGrader;
    private final ApplicationEventPublisher eventPublisher;

    @Transactional
    public SubmitAnswerResponse submitAnswer(SubmitAnswerRequest request) {
        // 개념-커버리지 채점: 제출 답안이 핵심 개념을 얼마나 짚었는지로 점수·정오답을 판정한다.
        GradeResult grade = answerGrader.grade(request.questionId(), request.submittedText());
        boolean isCorrect = grade.correct();
        int score = grade.score();
        log.info("[답안 수신] user={}, question={}, 점수={} ({}) → 이벤트 발행(AFTER_COMMIT 후 오답노트·통계·메일)",
                request.userEmail(), request.questionId(), score, isCorrect ? "정답" : "오답");

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
                saved.getResponseTimeMs(),
                grade
        ));

        return new SubmitAnswerResponse(
                saved.getId(),
                saved.isCorrect(),
                saved.getScore(),
                feedbackMessage(grade)
        );
    }

    private String feedbackMessage(GradeResult grade) {
        if (grade.correct()) {
            return String.format("정답입니다! (%d점) 결과를 메일로 보내드렸어요.", grade.score());
        }
        return String.format("오답입니다 (%d점). 오답노트에 담고 피드백을 메일로 보내드렸어요.", grade.score());
    }
}
