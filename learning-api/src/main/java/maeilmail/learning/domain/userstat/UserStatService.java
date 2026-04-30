package maeilmail.learning.domain.userstat;

import lombok.RequiredArgsConstructor;
import maeilmail.learning.domain.userstat.dto.UserStatDto;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserStatService {

    private final UserStatRepository userStatRepository;

    @Transactional
    public void recordAnswer(String userEmail, boolean isCorrect, long responseTimeMs) {
        UserStat stat = findOrCreateWithLock(userEmail);
        stat.recordAnswer(isCorrect, responseTimeMs);
    }

    @Transactional(readOnly = true)
    public UserStatDto findByEmail(String userEmail) {
        UserStat stat = userStatRepository.findByUserEmail(userEmail)
                .orElseThrow(() -> new IllegalArgumentException("사용자 통계를 찾을 수 없습니다. email=" + userEmail));
        return UserStatDto.from(stat);
    }

    // PESSIMISTIC_WRITE 로 행 잠금 후 갱신 — 동시 생성 충돌은 unique constraint 로 감지 후 재조회
    private UserStat findOrCreateWithLock(String userEmail) {
        try {
            return userStatRepository.findByUserEmailForUpdate(userEmail)
                    .orElseGet(() -> userStatRepository.saveAndFlush(UserStat.create(userEmail)));
        } catch (DataIntegrityViolationException e) {
            return userStatRepository.findByUserEmailForUpdate(userEmail)
                    .orElseThrow(() -> new IllegalStateException("UserStat 생성 실패: " + userEmail));
        }
    }
}
