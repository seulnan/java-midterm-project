package maeilmail.learning.domain.userstat;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface UserStatRepository extends JpaRepository<UserStat, Long> {

    Optional<UserStat> findByUserEmail(String userEmail);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT u FROM UserStat u WHERE u.userEmail = :userEmail")
    Optional<UserStat> findByUserEmailForUpdate(@Param("userEmail") String userEmail);
}
