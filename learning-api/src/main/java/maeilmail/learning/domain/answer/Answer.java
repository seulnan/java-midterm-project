package maeilmail.learning.domain.answer;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "answers")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Answer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String userEmail;

    @Column(nullable = false)
    private Long questionId;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String submittedText;

    @Column(nullable = false)
    private boolean correct;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private long responseTimeMs;

    @CreatedDate
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public Answer(String userEmail, Long questionId, String submittedText, boolean correct, int score, long responseTimeMs) {
        this.userEmail = userEmail;
        this.questionId = questionId;
        this.submittedText = submittedText;
        this.correct = correct;
        this.score = score;
        this.responseTimeMs = responseTimeMs;
    }
}
