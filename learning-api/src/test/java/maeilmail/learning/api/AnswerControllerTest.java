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
    void POST_answers_submittedText_빈_문자열_400() throws Exception {
        String badJson = """
                {"questionId": 1, "submittedText": "", "responseTimeMs": 1000, "userEmail": "u@t.com"}
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
