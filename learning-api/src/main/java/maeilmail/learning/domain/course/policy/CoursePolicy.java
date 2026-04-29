package maeilmail.learning.domain.course.policy;

import java.util.List;
import maeilmail.learning.domain.course.CourseType;

public interface CoursePolicy {

    CourseType courseType();

    List<Long> recommendQuestionIds(String userEmail, int limit);
}
