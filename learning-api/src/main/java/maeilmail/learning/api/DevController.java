package maeilmail.learning.api;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import maeilmail.learning.common.ApiResponse;
import maeilmail.learning.infrastructure.mail.LearningMailSender;
import maeilmail.learning.infrastructure.mail.MockMailSender;
import org.springframework.context.annotation.Profile;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/dev")
@RequiredArgsConstructor
@Profile({"local", "test", "dev"})
public class DevController {

    private final LearningMailSender mailSender;

    @PostMapping("/test-mail")
    public ApiResponse<Map<String, Object>> testMail(@RequestBody Map<String, String> body) {
        String to = body.getOrDefault("to", "test@example.com");
        String subject = body.getOrDefault("subject", "테스트 메일");
        mailSender.send(to, subject, "learning-api 메일 발송 테스트");

        String log = mailSender instanceof MockMailSender mock
                ? mock.getSentLogs().get(mock.getSentLogs().size() - 1)
                : String.format("SMTP sent to=%s subject=%s", to, subject);
        return ApiResponse.ok(Map.of("sent", true, "log", log));
    }
}
