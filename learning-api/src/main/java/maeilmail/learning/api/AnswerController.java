package maeilmail.learning.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import maeilmail.learning.common.ApiResponse;
import maeilmail.learning.domain.answer.AnswerService;
import maeilmail.learning.domain.answer.dto.SubmitAnswerRequest;
import maeilmail.learning.domain.answer.dto.SubmitAnswerResponse;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/answers")
@RequiredArgsConstructor
public class AnswerController {

    private final AnswerService answerService;

    @PostMapping
    public ApiResponse<SubmitAnswerResponse> submit(@Valid @RequestBody SubmitAnswerRequest request) {
        return ApiResponse.ok(answerService.submitAnswer(request));
    }
}
