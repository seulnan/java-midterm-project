package maeilmail.learning.domain.userstat.dto;

import maeilmail.learning.domain.userstat.Difficulty;
import maeilmail.learning.domain.userstat.UserStat;

public record UserStatDto(
        String userEmail,
        int totalAttempts,
        int correctCount,
        double accuracyRate,
        Difficulty currentDifficulty,
        long avgResponseTimeMs
) {
    public static UserStatDto from(UserStat stat) {
        double accuracy = stat.getTotalAttempts() == 0 ? 0.0
                : (double) stat.getCorrectCount() / stat.getTotalAttempts();
        return new UserStatDto(
                stat.getUserEmail(),
                stat.getTotalAttempts(),
                stat.getCorrectCount(),
                accuracy,
                stat.getCurrentDifficulty(),
                stat.getAvgResponseTimeMs()
        );
    }
}
