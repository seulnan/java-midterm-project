package maeilmail.learning.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import maeilmail.learning.common.exception.ResourceNotFoundException;
import maeilmail.learning.domain.wrongnote.WrongNoteService;
import maeilmail.learning.domain.wrongnote.dto.ReviewRequest;
import maeilmail.learning.domain.wrongnote.dto.WrongNoteDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(WrongNoteController.class)
class WrongNoteControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private WrongNoteService wrongNoteService;

    @Test
    void GET_wrong_notes_me_페이지_반환() throws Exception {
        WrongNoteDto dto = new WrongNoteDto(1L, 5L, 0, 2.5, 1, LocalDateTime.now().plusDays(1), null);
        given(wrongNoteService.findMyNotes(eq("u@t.com"), any(PageRequest.class)))
                .willReturn(new PageImpl<>(List.of(dto)));

        mockMvc.perform(get("/api/wrong-notes/me")
                        .param("email", "u@t.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.content[0].questionId").value(5));
    }

    @Test
    void GET_wrong_notes_me_due_복습_대상_반환() throws Exception {
        WrongNoteDto dto = new WrongNoteDto(2L, 7L, 1, 2.4, 1, LocalDateTime.now().minusDays(1), LocalDateTime.now().minusDays(2));
        given(wrongNoteService.findDueNotes("u@t.com")).willReturn(List.of(dto));

        mockMvc.perform(get("/api/wrong-notes/me/due").param("email", "u@t.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(1))
                .andExpect(jsonPath("$.data[0].questionId").value(7));
    }

    @Test
    void POST_wrong_notes_id_review_정답_처리() throws Exception {
        WrongNoteDto dto = new WrongNoteDto(1L, 5L, 1, 2.6, 2, LocalDateTime.now().plusDays(2), LocalDateTime.now());
        given(wrongNoteService.review(eq(1L), any(ReviewRequest.class))).willReturn(dto);

        mockMvc.perform(post("/api/wrong-notes/1/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReviewRequest(true))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewCount").value(1))
                .andExpect(jsonPath("$.data.intervalDays").value(2));
    }

    @Test
    void POST_wrong_notes_id_review_존재하지_않는_ID_404() throws Exception {
        given(wrongNoteService.review(eq(999L), any()))
                .willThrow(new ResourceNotFoundException("오답노트를 찾을 수 없습니다."));

        mockMvc.perform(post("/api/wrong-notes/999/review")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(new ReviewRequest(false))))
                .andExpect(status().isNotFound());
    }
}
