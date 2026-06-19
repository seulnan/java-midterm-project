package maeilmail.learning.config;

import lombok.extern.slf4j.Slf4j;
import maeilmail.learning.infrastructure.mail.LearningMailSender;
import maeilmail.learning.infrastructure.mail.MockMailSender;
import maeilmail.learning.infrastructure.mail.SmtpMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

/**
 * 메일 발송기 선택 — 별도 flag 없이 SMTP 사용자명(spring.mail.username) 유무로 자동 결정한다.
 *
 * <p>.env를 source해 SMTP_USERNAME이 채워지면 → SmtpMailSender(실제 발송),
 * 비어 있으면 → MockMailSender(콘솔 로그만). 로컬은 application-local.yml에서
 * spring.mail.username을 {@code ${SMTP_USERNAME:}}로 두므로, source 여부가 곧 발송 모드가 된다.
 */
@Slf4j
@Configuration
class MailConfig {

    @Bean
    @ConditionalOnExpression("'${spring.mail.username:}'.trim().isEmpty()")
    public LearningMailSender mockMailSender() {
        log.info("📧 메일 발송기 = MockMailSender (콘솔 로그만, 실제 발송 안 함). "
                + "실제 발송하려면 .env를 source 후 재기동하세요: set -a; source .env; set +a");
        return new MockMailSender();
    }

    @Bean
    @ConditionalOnExpression("!'${spring.mail.username:}'.trim().isEmpty()")
    public LearningMailSender smtpMailSender(
            JavaMailSender javaMailSender,
            @Value("${spring.mail.username}") String fromAddress) {
        log.info("📧 메일 발송기 = SmtpMailSender (실제 발송, from={})", fromAddress);
        return new SmtpMailSender(javaMailSender, fromAddress);
    }
}
