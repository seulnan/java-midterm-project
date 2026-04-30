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
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class WrongNoteService {

    private final WrongNoteRepository wrongNoteRepository;

    @Transactional
    public void registerOrSkip(String userEmail, Long questionId) {
        wrongNoteRepository.findByUserEmailAndQuestionId(userEmail, questionId)
                .ifPresentOrElse(
                        note -> {}, // 이미 있으면 무시
                        () -> wrongNoteRepository.save(WrongNote.create(userEmail, questionId))
                );
    }

    @Transactional
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
