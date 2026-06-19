package maeilmail.learning.domain.review;

import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.learning.adapter.LegacyQuestion;
import maeilmail.learning.adapter.LegacyQuestionPort;
import maeilmail.learning.domain.answer.Answer;
import maeilmail.learning.domain.answer.AnswerRepository;
import maeilmail.learning.domain.grading.AnswerGrader;
import maeilmail.learning.domain.grading.GradeResult;
import maeilmail.learning.domain.review.dto.ReviewResponse;
import maeilmail.learning.domain.review.dto.ReviewResponse.Missed;
import maeilmail.learning.domain.review.dto.ReviewResponse.ReviewItem;
import maeilmail.learning.domain.wrongnote.WrongNote;
import maeilmail.learning.domain.wrongnote.WrongNoteRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 복습 화면 조립 — 오답노트의 문제마다 "내가 제출한 답안 · 점수 · 피드백(약점/모범답안) · 시도 이력"을 모은다.
 * 피드백은 최근 제출 답안을 다시 채점해 산출한다(저장된 점수와 동일 알고리즘).
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final WrongNoteRepository wrongNoteRepository;
    private final AnswerRepository answerRepository;
    private final AnswerGrader answerGrader;
    private final LegacyQuestionPort questionPort;

    public ReviewResponse getReview(String email) {
        List<Answer> all = answerRepository.findByUserEmailOrderByCreatedAtAsc(email);
        int correctCount = (int) all.stream().filter(Answer::isCorrect).count();

        List<ReviewItem> items = wrongNoteRepository.findByUserEmailOrderByNextReviewAtAsc(email).stream()
                .map(note -> toItem(email, note))
                .toList();

        return new ReviewResponse(email, all.size(), correctCount, items);
    }

    private ReviewItem toItem(String email, WrongNote note) {
        Long questionId = note.getQuestionId();
        LegacyQuestion question = questionPort.findById(questionId)
                .orElseGet(() -> new LegacyQuestion(questionId, "제출한 문제", "", "GENERAL"));

        List<Answer> attempts = answerRepository.findByUserEmailAndQuestionIdOrderByCreatedAtAsc(email, questionId);
        Answer latest = attempts.isEmpty() ? null : attempts.get(attempts.size() - 1);
        String myAnswer = latest == null ? "" : latest.getSubmittedText();
        int score = latest == null ? 0 : latest.getScore();
        List<Integer> attemptScores = attempts.stream().map(Answer::getScore).toList();

        GradeResult grade = answerGrader.grade(questionId, myAnswer);
        List<Missed> missed = grade.missed().stream()
                .map(c -> new Missed(c.label(), c.why()))
                .toList();

        return new ReviewItem(
                questionId, question.title(), question.content(), question.category(),
                myAnswer, score, missed, grade.modelAnswer(),
                attemptScores, attempts.size(), note.getNextReviewAt(), note.getIntervalDays());
    }
}
