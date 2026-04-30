package maeilmail.learning.adapter;

import java.util.List;
import java.util.Optional;

public interface LegacyQuestionPort {

    Optional<LegacyQuestion> findById(Long id);

    List<LegacyQuestion> findByIds(List<Long> ids);

    List<LegacyQuestion> findByCategory(String category);
}
