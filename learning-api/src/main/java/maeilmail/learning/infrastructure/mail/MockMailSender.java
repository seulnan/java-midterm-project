package maeilmail.learning.infrastructure.mail;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class MockMailSender implements LearningMailSender {

    private final List<String> sentLogs = new CopyOnWriteArrayList<>();

    @Override
    public void send(String to, String subject, String body) {
        String record = String.format("[MOCK MAIL] to=%s subject=%s", to, subject);
        sentLogs.add(record);
        log.info(record);
    }

    public List<String> getSentLogs() {
        return Collections.unmodifiableList(sentLogs);
    }

    public void clear() {
        sentLogs.clear();
    }
}
