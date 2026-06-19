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
    public String send(String to, String subject, String body) {
        return doSend(to, subject, body, null);
    }

    @Override
    public String sendReply(String to, String subject, String body, String inReplyToMessageId) {
        return doSend(to, subject, body, inReplyToMessageId);
    }

    // body는 QbitMailTemplate이 만든 HTML이다. inReplyTo가 있으면 같은 스레드의 답장으로 보낸다.
    private String doSend(String to, String subject, String body, String inReplyToMessageId) {
        try {
            MimeMessage message = javaMailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, false, "UTF-8");
            helper.setFrom(fromAddress, "QBit");
            helper.setTo(to);
            helper.setSubject(subject);
            helper.setText(body, true);
            if (inReplyToMessageId != null) {
                message.setHeader("In-Reply-To", inReplyToMessageId);
                message.setHeader("References", inReplyToMessageId);
            }
            log.info("[SMTP MAIL] to={} subject={}{}", to, subject, inReplyToMessageId != null ? " (reply)" : "");
            javaMailSender.send(message);
            return message.getMessageID();
        } catch (MailException e) {
            throw e;
        } catch (Exception e) {
            throw new MailSendException("메일 발송에 실패했습니다: " + e.getMessage(), e);
        }
    }
}
