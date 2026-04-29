package maeilmail.learning.domain.wrongnote.dto;

import java.time.LocalDateTime;
import maeilmail.learning.domain.wrongnote.WrongNote;

public record WrongNoteDto(
        Long id,
        Long questionId,
        int reviewCount,
        double easeFactor,
        int intervalDays,
        LocalDateTime nextReviewAt,
        LocalDateTime lastReviewedAt
) {
    public static WrongNoteDto from(WrongNote note) {
        return new WrongNoteDto(
                note.getId(),
                note.getQuestionId(),
                note.getReviewCount(),
                note.getEaseFactor(),
                note.getIntervalDays(),
                note.getNextReviewAt(),
                note.getLastReviewedAt()
        );
    }
}
