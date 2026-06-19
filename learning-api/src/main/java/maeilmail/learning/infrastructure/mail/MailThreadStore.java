package maeilmail.learning.infrastructure.mail;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.stereotype.Component;

/**
 * 질문 메일의 스레드 정보(Message-ID·제목)를 (사용자, 질문) 단위로 보관한다.
 * 답안 피드백을 같은 메일 스레드의 답장(Re:)으로 보내기 위함.
 *
 * <p>데모 범위에서는 인메모리로 충분하다(실서비스에선 영속 저장으로 교체 가능).
 */
@Component
public class MailThreadStore {

    public record ThreadRef(String messageId, String subject) {}

    private final ConcurrentHashMap<String, ThreadRef> store = new ConcurrentHashMap<>();

    public void remember(String userEmail, Long questionId, String messageId, String subject) {
        if (messageId == null) {
            return;
        }
        store.put(key(userEmail, questionId), new ThreadRef(messageId, subject));
    }

    public Optional<ThreadRef> find(String userEmail, Long questionId) {
        return Optional.ofNullable(store.get(key(userEmail, questionId)));
    }

    private String key(String userEmail, Long questionId) {
        return userEmail + "|" + questionId;
    }
}
