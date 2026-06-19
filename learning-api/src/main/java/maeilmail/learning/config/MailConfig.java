package maeilmail.learning.config;

import maeilmail.learning.infrastructure.mail.LearningMailSender;
import maeilmail.learning.infrastructure.mail.MockMailSender;
import maeilmail.learning.infrastructure.mail.SmtpMailSender;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;

@Configuration
class MailConfig {

    // 메일 구현체는 프로필이 아니라 learning.mail.sender 프로퍼티로 선택한다.
    // 기본값(미지정)은 mock → local/test는 별도 설정 없이 콘솔 로그.
    // smtp로 두면 dev 프로필(Supabase) 없이도 local(H2)에서 실제 Gmail 발송이 가능하다.
    @Bean
    @ConditionalOnProperty(name = "learning.mail.sender", havingValue = "mock", matchIfMissing = true)
    public LearningMailSender mockMailSender() {
        return new MockMailSender();
    }

    @Bean
    @ConditionalOnProperty(name = "learning.mail.sender", havingValue = "smtp")
    public LearningMailSender smtpMailSender(
            JavaMailSender javaMailSender,
            @Value("${spring.mail.username}") String fromAddress) {
        return new SmtpMailSender(javaMailSender, fromAddress);
    }
}
