package maeilmail.learning.domain.userstat;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserStatRepository extends JpaRepository<UserStat, Long> {

    Optional<UserStat> findByUserEmail(String userEmail);
}
