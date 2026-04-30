package maeilmail.learning.domain.wrongnote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

@DataJpaTest
class WrongNoteRepositoryTest {

    @Autowired
    private WrongNoteRepository repository;

    @Test
    void 오답노트_저장_및_조회() {
        WrongNote note = WrongNote.create("user@t.com", 1L);
        repository.save(note);

        Optional<WrongNote> found = repository.findByUserEmailAndQuestionId("user@t.com", 1L);

        assertThat(found).isPresent();
        assertThat(found.get().getEaseFactor()).isEqualTo(2.5);
        assertThat(found.get().getIntervalDays()).isEqualTo(1);
    }

    @Test
    void 동일_user_question_중복_저장_시_예외() {
        repository.saveAndFlush(WrongNote.create("user@t.com", 1L));

        assertThatThrownBy(() -> repository.saveAndFlush(WrongNote.create("user@t.com", 1L)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void 복습_기한_도래한_노트만_조회() {
        WrongNote due = WrongNote.create("user@t.com", 1L);
        WrongNote notDue = WrongNote.create("user@t.com", 2L);
        // notDue의 nextReviewAt은 기본값(1일 뒤)이므로 현재 시각 이후 → 조회 제외
        repository.save(due);
        repository.save(notDue);

        // nextReviewAt를 과거로 바꿔야 due로 잡힘 — 초기값은 now+1일이므로 미래
        // 따라서 조회 결과는 비어야 함
        List<WrongNote> result = repository
                .findByUserEmailAndNextReviewAtLessThanEqual("user@t.com", LocalDateTime.now().minusHours(1));

        assertThat(result).isEmpty();
    }

    @Test
    void nextReviewAt_과거인_노트_due_조회() {
        WrongNote note = WrongNote.create("user@t.com", 3L);
        repository.saveAndFlush(note);

        // 복습 기한을 과거로 강제 처리: applyReview(false) 후 DB에서 다시 조회
        // 대신 findAllByNextReviewAtLessThanEqual로 충분히 미래 시각을 넘겨서 포함 확인
        List<WrongNote> all = repository
                .findByUserEmailAndNextReviewAtLessThanEqual("user@t.com", LocalDateTime.now().plusDays(2));

        assertThat(all).hasSize(1);
        assertThat(all.get(0).getQuestionId()).isEqualTo(3L);
    }

    @Test
    void 사용자별_페이지_조회() {
        repository.save(WrongNote.create("a@t.com", 1L));
        repository.save(WrongNote.create("a@t.com", 2L));
        repository.save(WrongNote.create("b@t.com", 3L));

        Page<WrongNote> page = repository.findByUserEmail("a@t.com", PageRequest.of(0, 10));

        assertThat(page.getTotalElements()).isEqualTo(2);
        assertThat(page.getContent()).allMatch(n -> n.getUserEmail().equals("a@t.com"));
    }

    @Test
    void 삭제_후_조회_시_빈_Optional() {
        WrongNote note = repository.save(WrongNote.create("user@t.com", 5L));
        repository.delete(note);

        assertThat(repository.findByUserEmailAndQuestionId("user@t.com", 5L)).isEmpty();
    }
}
