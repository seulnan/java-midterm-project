package maeilmail.learning.infrastructure.mail;

public interface LearningMailSender {

    /** 메일을 보내고, 발송된 메시지의 Message-ID를 반환한다(스레드 답장용). Mock은 null. */
    String send(String to, String subject, String body);

    /** 주어진 Message-ID를 가리키는 답장(같은 스레드)으로 보낸다. Mock은 null. */
    String sendReply(String to, String subject, String body, String inReplyToMessageId);
}
