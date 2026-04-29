package maeilmail.learning.domain.userstat;

import lombok.RequiredArgsConstructor;
import maeilmail.learning.domain.userstat.dto.UserStatDto;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStatService {

    private final UserStatRepository userStatRepository;

    @Transactional
    public synchronized void recordAnswer(String userEmail, boolean isCorrect, long responseTimeMs) {
        UserStat stat = userStatRepository.findByUserEmail(userEmail)
                .orElseGet(() -> userStatRepository.save(UserStat.create(userEmail)));
        stat.recordAnswer(isCorrect, responseTimeMs);
    }

    @Transactional(readOnly = true)
    public UserStatDto findByEmail(String userEmail) {
        UserStat stat = userStatRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자 통계를 찾을 수 없습니다. email=" + userEmail));
        return UserStatDto.from(stat);
    }
}
