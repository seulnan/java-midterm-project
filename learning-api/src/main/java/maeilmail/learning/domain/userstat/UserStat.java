package maeilmail.learning.domain.userstat;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

@Entity
@Table(name = "user_stats")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class UserStat {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String userEmail;

    @Column(nullable = false)
    private int totalAttempts;

    @Column(nullable = false)
    private int correctCount;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Difficulty currentDifficulty;

    @Column(nullable = false)
    private long avgResponseTimeMs;

    @Column
    private LocalDateTime lastActiveAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    @Version
    @Column(nullable = false)
    private Long version;

    public static UserStat create(String userEmail) {
        UserStat stat = new UserStat();
        stat.userEmail = userEmail;
        stat.totalAttempts = 0;
        stat.correctCount = 0;
        stat.currentDifficulty = Difficulty.EASY;
        stat.avgResponseTimeMs = 0;
        stat.version = 0L;
        return stat;
    }

    // synchronized는 Service 계층에서 적용 — 단일 JVM 동시성 보장
    public synchronized void recordAnswer(boolean isCorrect, long responseTimeMs) {
        this.totalAttempts++;
        if (isCorrect) {
            this.correctCount++;
        }
        // 지수 이동 평균으로 응답 시간 갱신
        this.avgResponseTimeMs = (long) (avgResponseTimeMs * 0.8 + responseTimeMs * 0.2);
        this.lastActiveAt = LocalDateTime.now();
        adjustDifficulty();
    }

    private void adjustDifficulty() {
        if (totalAttempts < 20) {
            return;
        }
        // 최근 20문제를 전체 정답률로 근사 (정확한 최근 20개는 Repository 쿼리 필요)
        double recentAccuracy = (double) correctCount / totalAttempts;
        if (recentAccuracy > 0.8) {
            this.currentDifficulty = currentDifficulty.upgrade();
        } else if (recentAccuracy < 0.4) {
            this.currentDifficulty = currentDifficulty.downgrade();
        }
    }
}
