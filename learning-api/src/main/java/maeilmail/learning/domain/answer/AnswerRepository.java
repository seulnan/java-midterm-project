package maeilmail.learning.domain.answer;

import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AnswerRepository extends JpaRepository<Answer, Long> {

    List<Answer> findTop20ByUserEmailOrderByCreatedAtDesc(String userEmail);
}
