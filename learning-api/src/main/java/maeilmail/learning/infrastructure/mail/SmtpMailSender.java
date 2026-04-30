package maeilmail.learning.infrastructure.mail;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

@Slf4j
@RequiredArgsConstructor
public class SmtpMailSender implements LearningMailSender {

    private final JavaMailSender javaMailSender;
    private final String fromAddress;

    @Override
    public void send(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromAddress);
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        log.info("[SMTP MAIL] to={} subject={}", to, subject);
        javaMailSender.send(message);
    }
}
