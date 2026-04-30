package maeilmail.learning.config;

import maeilmail.learning.infrastructure.mail.LearningMailSender;
import maeilmail.learning.infrastructure.mail.MockMailSender;
import maeilmail.learning.infrastructure.mail.SmtpMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
class MailConfig {

    @Bean
    @Profile("!dev")
    public LearningMailSender mockMailSender() {
        return new MockMailSender();
    }

    @Bean
    @Profile("dev")
    public LearningMailSender smtpMailSender(
            JavaMailSender javaMailSender,
            @Value("${spring.mail.username}") String fromAddress) {
        return new SmtpMailSender(javaMailSender, fromAddress);
    }
}
