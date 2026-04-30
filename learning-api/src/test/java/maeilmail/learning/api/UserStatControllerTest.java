package maeilmail.learning.api;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import maeilmail.learning.common.exception.ResourceNotFoundException;
import maeilmail.learning.domain.userstat.Difficulty;
import maeilmail.learning.domain.userstat.UserStatService;
import maeilmail.learning.domain.userstat.dto.UserStatDto;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(UserStatController.class)
class UserStatControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private UserStatService userStatService;

    @Test
    void GET_stats_me_정상_반환() throws Exception {
        UserStatDto dto = new UserStatDto("user@t.com", 10, 8, 0.8, Difficulty.MEDIUM, 1200L);
        given(userStatService.findByEmail("user@t.com")).willReturn(dto);

        mockMvc.perform(get("/api/stats/me").param("email", "user@t.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.userEmail").value("user@t.com"))
                .andExpect(jsonPath("$.data.currentDifficulty").value("MEDIUM"))
                .andExpect(jsonPath("$.data.totalAttempts").value(10));
    }

    @Test
    void GET_stats_me_존재하지_않는_사용자_404() throws Exception {
        given(userStatService.findByEmail("ghost@t.com"))
                .willThrow(new ResourceNotFoundException("사용자 통계를 찾을 수 없습니다."));

        mockMvc.perform(get("/api/stats/me").param("email", "ghost@t.com"))
                .andExpect(status().isNotFound());
    }

    @Test
    void GET_stats_me_email_파라미터_누락_400() throws Exception {
        mockMvc.perform(get("/api/stats/me"))
                .andExpect(status().isBadRequest());
    }
}
