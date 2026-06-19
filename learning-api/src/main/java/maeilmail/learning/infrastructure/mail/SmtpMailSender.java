package maeilmail.learning.infrastructure.mail;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.MailException;
import org.springframework.mail.MailSendException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;

@Slf4j
@RequiredArgsConstructor
public class SmtpMailSender implements LearningMailSender {

    private final JavaMailSender javaMailSender;
    private final String fromAddress;

    @Override
    public void send(String to, String subject, String body) {
        // body는 QbitMailTemplate이 만든 HTML이다. MimeMessage + setText(html=true)로 발송한다.
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress, "QBit");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            log.info("[SMTP MAIL] to={} subject={}", to, subject);
            javaMailSender.send(message);
        } catch (MailException e) {
            throw e;
        } catch (Exception e) {
            throw new MailSendException("메일 발송에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
