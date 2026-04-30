package maeilmail.learning.infrastructure.mail;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MockMailSenderTest {

    private MockMailSender sender;

    @BeforeEach
    void setUp() {
        sender = new MockMailSender();
    }

    @Test
    void 메일_발송_시_로그_기록() {
        sender.send("user@test.com", "테스트 제목", "본문");
        assertThat(sender.getSentLogs()).hasSize(1);
        assertThat(sender.getSentLogs().get(0)).contains("user@test.com").contains("테스트 제목");
    }

    @Test
    void 복수_메일_발송_후_로그_누적() {
        sender.send("a@test.com", "제목1", "본문1");
        sender.send("b@test.com", "제목2", "본문2");
        assertThat(sender.getSentLogs()).hasSize(2);
    }

    @Test
    void clear_후_로그_비워짐() {
        sender.send("user@test.com", "제목", "본문");
        sender.clear();
        assertThat(sender.getSentLogs()).isEmpty();
    }
}
