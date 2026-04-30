package maeilmail.learning.domain.wrongnote;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(
        name = "wrong_notes",
        uniqueConstraints = @UniqueConstraint(
                name = "uq_wrong_notes_user_question",
                columnNames = {"user_email", "question_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class WrongNote {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private Long questionId;

    @Column
    private LocalDateTime lastReviewedAt;

    @Column(nullable = false)
    private LocalDateTime nextReviewAt;

    @Column(nullable = false)
    private int reviewCount;

    @Column(nullable = false)
    private double easeFactor;

    @Column(nullable = false)
    private int intervalDays;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public static WrongNote create(String userEmail, Long questionId) {
        WrongNote note = new WrongNote();
        note.userEmail = userEmail;
        note.questionId = questionId;
        note.reviewCount = 0;
        note.easeFactor = 2.5;
        note.intervalDays = 1;
        note.nextReviewAt = LocalDateTime.now().plusDays(1);
        return note;
    }

    public void applyReview(boolean isCorrect) {
        LocalDateTime now = LocalDateTime.now();
        this.lastReviewedAt = now;
        this.reviewCount++;

        if (isCorrect) {
            int newInterval = Math.max(1, (int) Math.round(intervalDays * easeFactor));
            this.easeFactor = easeFactor + 0.1;
            this.intervalDays = newInterval;
        } else {
            this.intervalDays = 1;
            this.easeFactor = Math.max(1.3, easeFactor - 0.2);
        }
        this.nextReviewAt = now.plusDays(intervalDays);
    }
}
