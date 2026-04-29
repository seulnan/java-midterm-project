package maeilmail.learning.infrastructure.mail;

public interface LearningMailSender {

    void send(String to, String subject, String body);
}
