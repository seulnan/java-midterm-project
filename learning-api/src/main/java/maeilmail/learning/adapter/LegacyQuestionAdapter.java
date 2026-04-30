package maeilmail.learning.adapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.springframework.stereotype.Component;

/**
 * mail-core의 Question 엔티티를 learning-api 도메인으로 변환하는 어댑터.
 *
 * <p>Adapter 패턴: LegacyQuestionPort(Target)와 mail-core Question(Adaptee) 사이의 번역 계층.
 * learning-api는 LegacyQuestionPort에만 의존하고, 실제 데이터 소스는 이 어댑터가 캡슐화한다.
 *
 * <p>현재 구현은 인메모리 스텁으로 mail-core Question 구조를 모방한다.
 * 공유 스프링 컨텍스트 배포 시 maeilmail.question.QuestionRepository를 직접 주입하여
 * 실제 DB 데이터를 반환하도록 교체 가능하다.
 */
@Component
public class LegacyQuestionAdapter implements LegacyQuestionPort {

    private final Map<Long, LegacyQuestion> store;

    public LegacyQuestionAdapter() {
        this.store = buildInMemoryStore();
    }

    @Override
    public Optional<LegacyQuestion> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<LegacyQuestion> findByIds(List<Long> ids) {
        return ids.stream()
                .map(store::get)
                .filter(q -> q != null)
                .collect(Collectors.toList());
    }

    @Override
    public List<LegacyQuestion> findByCategory(String category) {
        return store.values().stream()
                .filter(q -> q.category().equalsIgnoreCase(category))
                .collect(Collectors.toList());
    }

    private Map<Long, LegacyQuestion> buildInMemoryStore() {
        return LongStream.rangeClosed(1, 200)
                .mapToObj(id -> new LegacyQuestion(
                        id,
                        "Question #" + id,
                        "Content for question " + id,
                        id % 2 == 0 ? "BACKEND" : "FRONTEND"
                ))
                .collect(Collectors.toMap(LegacyQuestion::id, q -> q));
    }
}
