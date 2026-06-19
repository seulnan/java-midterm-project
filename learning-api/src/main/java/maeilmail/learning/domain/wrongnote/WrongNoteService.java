package maeilmail.learning.domain.wrongnote;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.learning.common.exception.ResourceNotFoundException;
import maeilmail.learning.domain.wrongnote.dto.ReviewRequest;
import maeilmail.learning.domain.wrongnote.dto.WrongNoteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WrongNoteService {

    private final WrongNoteRepository wrongNoteRepository;

    public void registerOrSkip(String userEmail, Long questionId) {
        registerOrSkip(userEmail, questionId, null);
    }

    // AFTER_COMMIT 동기 리스너에서 호출된다. 이 시점엔 직전 트랜잭션이 이미 커밋됐지만
    // 스레드에 동기화가 남아 있어, REQUIRED면 새 물리 트랜잭션을 열지 않아 save가 커밋되지 않는다.
    // REQUIRES_NEW로 독립 트랜잭션을 강제해 오답노트 등록/삭제가 확실히 반영되게 한다.
    // weakness: 채점에서 놓친 핵심 개념(왜 오답인지)을 함께 기록한다.
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void registerOrSkip(String userEmail, Long questionId, String weakness) {
        wrongNoteRepository.findByUserEmailAndQuestionId(userEmail, questionId)
                .ifPresentOrElse(
                        note -> note.recordWeakness(weakness), // 이미 있으면 약점만 최신화
                        () -> {
                            WrongNote note = WrongNote.create(userEmail, questionId);
                            note.recordWeakness(weakness);
                            wrongNoteRepository.save(note);
                        }
                );
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeIfExists(String userEmail, Long questionId) {
        wrongNoteRepository.findByUserEmailAndQuestionId(userEmail, questionId)
                .ifPresent(wrongNoteRepository::delete);
    }

    @Transactional(readOnly = true)
    public Page<WrongNoteDto> findMyNotes(String userEmail, Pageable pageable) {
        return wrongNoteRepository.findByUserEmail(userEmail, pageable)
                .map(WrongNoteDto::from);
    }

    @Transactional(readOnly = true)
    public List<WrongNoteDto> findDueNotes(String userEmail) {
        return wrongNoteRepository
                .findByUserEmailAndNextReviewAtLessThanEqual(userEmail, LocalDateTime.now())
                .stream()
                .map(WrongNoteDto::from)
                .toList();
    }

    @Transactional
    public WrongNoteDto review(Long id, ReviewRequest request) {
        WrongNote note = wrongNoteRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("오답노트를 찾을 수 없습니다. id=" + id));
        note.applyReview(request.isCorrect());
        return WrongNoteDto.from(note);
    }
}
