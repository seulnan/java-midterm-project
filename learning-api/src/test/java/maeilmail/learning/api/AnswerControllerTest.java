package maeilmail.learning.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import maeilmail.learning.domain.answer.AnswerService;
import maeilmail.learning.domain.answer.dto.SubmitAnswerRequest;
import maeilmail.learning.domain.answer.dto.SubmitAnswerResponse;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AnswerController.class)
class AnswerControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AnswerService answerService;

    @Test
    void POST_answers_정상_요청() throws Exception {
        SubmitAnswerRequest request = new SubmitAnswerRequest(1L, "정답 내용", 1500L, "user@test.com");
        given(answerService.submitAnswer(any())).willReturn(new SubmitAnswerResponse(1L, true, 90, "정답입니다"));

        mockMvc.perform(post("/api/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.isCorrect").value(true))
                .andExpect(jsonPath("$.data.score").value(90));
    }

    @Test
    void POST_answers_userEmail_누락_시_400() throws Exception {
        String badJson = """
                {"questionId": 1, "submittedText": "답변", "responseTimeMs": 1000}
                """;

        mockMvc.perform(post("/api/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_answers_submittedText_빈_문자열은_오답으로_접수된다() throws Exception {
        // 빈 문자열("")은 미작성/오답으로 간주해 채점 흐름을 타야 한다(400이 아니라 201 + isCorrect=false).
        String json = """
                {"questionId": 1, "submittedText": "", "responseTimeMs": 1000, "userEmail": "u@t.com"}
                """;
        given(answerService.submitAnswer(any()))
                .willReturn(new SubmitAnswerResponse(1L, false, 0, "오답입니다. 오답노트에 추가됩니다."));

        mockMvc.perform(post("/api/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.isCorrect").value(false))
                .andExpect(jsonPath("$.data.score").value(0));
    }

    @Test
    void POST_answers_submittedText_누락_시_400() throws Exception {
        // null은 여전히 거부(@NotNull) — 서비스의 isBlank() NPE를 막는다.
        String badJson = """
                {"questionId": 1, "responseTimeMs": 1000, "userEmail": "u@t.com"}
                """;

        mockMvc.perform(post("/api/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void POST_answers_questionId_null_400() throws Exception {
        String badJson = """
                {"submittedText": "답변", "responseTimeMs": 1000, "userEmail": "u@t.com"}
                """;

        mockMvc.perform(post("/api/answers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }
}
