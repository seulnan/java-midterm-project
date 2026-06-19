package maeilmail.learning.api;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import maeilmail.learning.adapter.LegacyQuestion;
import maeilmail.learning.adapter.LegacyQuestionPort;
import maeilmail.learning.common.ApiResponse;
import maeilmail.learning.common.exception.ResourceNotFoundException;
import maeilmail.learning.infrastructure.mail.LearningMailSender;
import maeilmail.learning.infrastructure.mail.QbitMailTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 면접 질문 조회 + "오늘의 질문" 메일 발송.
 *
 * <p>데모 흐름의 시작점 — 사용자에게 질문을 메일로 보내고, 답안 페이지에서 그 질문을 보여줄 때 쓴다.
 */
@Slf4j
@RestController
@RequestMapping("/api/questions")
@RequiredArgsConstructor
public class QuestionController {

    private final LegacyQuestionPort questionPort;
    private final LearningMailSender mailSender;

    /** 답안 페이지가 질문 본문을 그릴 때 호출한다. */
    @GetMapping("/{id}")
    public ApiResponse<LegacyQuestion> getQuestion(@PathVariable Long id) {
        LegacyQuestion question = questionPort.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("질문을 찾을 수 없습니다. id=" + id));
        return ApiResponse.ok(question);
    }

    /** "오늘의 질문"을 사용자 메일로 발송한다(메일 안 버튼 → 답안 페이지). */
    @PostMapping("/deliver")
    public ApiResponse<DeliverResult> deliver(@RequestBody DeliverRequest request) {
        LegacyQuestion question = questionPort.findById(request.questionId())
                .orElseThrow(() -> new ResourceNotFoundException("질문을 찾을 수 없습니다. id=" + request.questionId()));

        QbitMailTemplate.Mail mail = QbitMailTemplate.questionDelivery(question, request.userEmail());
        log.info("[질문 메일] 발송: user={}, question={}({})", request.userEmail(), question.id(), question.title());
        mailSender.send(request.userEmail(), mail.subject(), mail.html());

        return ApiResponse.ok(new DeliverResult(question.id(), question.title(), request.userEmail()));
    }

    public record DeliverRequest(String userEmail, Long questionId) {}

    public record DeliverResult(Long questionId, String title, String sentTo) {}
}
