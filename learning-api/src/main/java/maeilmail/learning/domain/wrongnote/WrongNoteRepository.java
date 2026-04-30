package maeilmail.learning.domain.wrongnote;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface WrongNoteRepository extends JpaRepository<WrongNote, Long> {

    Optional<WrongNote> findByUserEmailAndQuestionId(String userEmail, Long questionId);

    Page<WrongNote> findByUserEmail(String userEmail, Pageable pageable);

    List<WrongNote> findByUserEmailAndNextReviewAtLessThanEqual(String userEmail, LocalDateTime now);

    List<WrongNote> findAllByNextReviewAtLessThanEqual(LocalDateTime now);
}
