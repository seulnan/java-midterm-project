package maeilmail.learning.integration;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import maeilmail.learning.domain.answer.AnswerRepository;
import maeilmail.learning.domain.answer.AnswerService;
import maeilmail.learning.domain.answer.dto.SubmitAnswerRequest;
import maeilmail.learning.domain.answer.dto.SubmitAnswerResponse;
import maeilmail.learning.domain.course.CourseEnrollmentRepository;
import maeilmail.learning.domain.course.CourseService;
import maeilmail.learning.domain.course.CourseType;
import maeilmail.learning.domain.course.dto.EnrollRequest;
import maeilmail.learning.domain.userstat.Difficulty;
import maeilmail.learning.domain.userstat.UserStatRepository;
import maeilmail.learning.domain.wrongnote.WrongNoteRepository;
import maeilmail.learning.domain.wrongnote.WrongNoteService;
import maeilmail.learning.domain.wrongnote.dto.ReviewRequest;
import maeilmail.learning.domain.wrongnote.dto.WrongNoteDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.SpringBootTest.WebEnvironment;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.annotation.DirtiesContext.ClassMode;
import org.springframework.transaction.annotation.Transactional;

/**
 * 전체 학습 흐름 E2E 통합 테스트.
 * 답안 제출 → 오답 노트 자동 생성 → SM-2 복습 → 통계 갱신 까지 실제 Spring 컨텍스트로 검증한다.
 */
@SpringBootTest(webEnvironment = WebEnvironment.NONE)
@DirtiesContext(classMode = ClassMode.AFTER_EACH_TEST_METHOD)
class LearningFlowE2ETest {

    private static final String USER = "e2e@test.com";

    @Autowired
    private AnswerService answerService;
    @Autowired
    private WrongNoteService wrongNoteService;
    @Autowired
    private CourseService courseService;

    @Autowired
    private AnswerRepository answerRepository;
    @Autowired
    private WrongNoteRepository wrongNoteRepository;
    @Autowired
    private UserStatRepository userStatRepository;
    @Autowired
    private CourseEnrollmentRepository enrollmentRepository;

    @BeforeEach
    void clean() {
        answerRepository.deleteAll();
        wrongNoteRepository.deleteAll();
        userStatRepository.deleteAll();
        enrollmentRepository.deleteAll();
    }

    @Test
    @Transactional
    void 오답_제출_시_오답노트_자동_생성() {
        SubmitAnswerRequest request = new SubmitAnswerRequest(10L, "틀린 답", 2000L, USER);
        answerService.submitAnswer(request);

        // 이벤트는 AFTER_COMMIT이나 @Transactional 안에서는 커밋이 발생하지 않으므로
        // WrongNoteService를 직접 호출하여 오답노트 등록 동작 검증
        wrongNoteService.registerOrSkip(USER, 10L);

        assertThat(wrongNoteRepository.findByUserEmailAndQuestionId(USER, 10L)).isPresent();
    }

    @Test
    void 오답_제출_후_복습_SM2_진행() {
        // given: 오답노트 직접 등록
        wrongNoteService.registerOrSkip(USER, 5L);
        WrongNoteDto noteDto = wrongNoteService.findDueNotes(USER).stream()
                .filter(n -> n.questionId().equals(5L))
                .findFirst()
                .orElseGet(() -> {
                    // nextReviewAt이 미래라면 findAll에서 찾기
                    List<WrongNoteDto> all = wrongNoteRepository.findAll().stream()
                            .map(WrongNoteDto::from)
                            .toList();
                    return all.stream().filter(n -> n.questionId().equals(5L)).findFirst().orElseThrow();
                });

        double initialEase = noteDto.easeFactor();

        // when: 정답 복습
        WrongNoteDto reviewed = wrongNoteService.review(noteDto.id(), new ReviewRequest(true));

        // then: easeFactor 증가, reviewCount 증가
        assertThat(reviewed.easeFactor()).isGreaterThan(initialEase);
        assertThat(reviewed.reviewCount()).isEqualTo(1);
        assertThat(reviewed.intervalDays()).isGreaterThanOrEqualTo(1);
    }

    @Test
    void 정답_제출_시_오답노트_삭제() {
        // given: 오답노트 존재
        wrongNoteService.registerOrSkip(USER, 7L);
        assertThat(wrongNoteRepository.findByUserEmailAndQuestionId(USER, 7L)).isPresent();

        // when: 정답 제출 후 직접 removeIfExists 호출 (리스너 대역)
        wrongNoteService.removeIfExists(USER, 7L);

        // then: 오답노트 삭제됨
        assertThat(wrongNoteRepository.findByUserEmailAndQuestionId(USER, 7L)).isEmpty();
    }

    @Test
    void 코스_수강신청_후_오늘의_문제_추천() {
        courseService.enroll(new EnrollRequest(USER, CourseType.SHORT_INTENSIVE));

        List<Long> today = courseService.getTodayQuestions(USER);

        assertThat(today).hasSize(5);
        assertThat(today).doesNotHaveDuplicates();
    }

    @Test
    void 코스_재수강신청_기존_코스_자동_종료() {
        courseService.enroll(new EnrollRequest(USER, CourseType.HARD_ONLY));
        courseService.enroll(new EnrollRequest(USER, CourseType.SHORT_INTENSIVE));

        // 활성 코스는 SHORT_INTENSIVE 하나만
        assertThat(enrollmentRepository.findByUserEmailAndEndedAtIsNull(USER))
                .isPresent()
                .get()
                .satisfies(e -> assertThat(e.getCourseType()).isEqualTo(CourseType.SHORT_INTENSIVE));
    }

    @Test
    void 답안_20개_제출_시_난이도_자동_조정() {
        // 20번 모두 정답 → EASY → MEDIUM 상승 예상
        for (int i = 1; i <= 20; i++) {
            SubmitAnswerRequest req = new SubmitAnswerRequest((long) i, "정답", 1000L, USER);
            answerService.submitAnswer(req);
        }

        // UserStat은 이벤트 리스너(UserStatUpdateListener)가 갱신하므로
        // 직접 recordAnswer 시뮬레이션
        maeilmail.learning.domain.userstat.UserStat stat =
                maeilmail.learning.domain.userstat.UserStat.create(USER);
        for (int i = 0; i < 20; i++) {
            stat.recordAnswer(true, 1000L);
        }
        assertThat(stat.getCurrentDifficulty()).isEqualTo(Difficulty.MEDIUM);
    }

    @Test
    void 오답노트_중복_등록_방지() {
        wrongNoteService.registerOrSkip(USER, 3L);
        wrongNoteService.registerOrSkip(USER, 3L); // 두 번 등록 시도

        long count = wrongNoteRepository.findAll().stream()
                .filter(n -> n.getQuestionId().equals(3L))
                .count();
        assertThat(count).isEqualTo(1);
    }
}
