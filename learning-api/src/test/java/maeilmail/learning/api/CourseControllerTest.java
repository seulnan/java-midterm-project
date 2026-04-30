package maeilmail.learning.api;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import maeilmail.learning.common.exception.ResourceNotFoundException;
import maeilmail.learning.domain.course.CourseService;
import maeilmail.learning.domain.course.CourseType;
import maeilmail.learning.domain.course.dto.CourseEnrollmentDto;
import maeilmail.learning.domain.course.dto.EnrollRequest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CourseController.class)
class CourseControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CourseService courseService;

    @Test
    void POST_courses_enroll_정상() throws Exception {
        CourseEnrollmentDto dto = new CourseEnrollmentDto(1L, "u@t.com", CourseType.SHORT_INTENSIVE, null, true);
        given(courseService.enroll(any())).willReturn(dto);

        EnrollRequest request = new EnrollRequest("u@t.com", CourseType.SHORT_INTENSIVE);

        mockMvc.perform(post("/api/courses/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.courseType").value("SHORT_INTENSIVE"))
                .andExpect(jsonPath("$.data.active").value(true));
    }

    @Test
    void POST_courses_enroll_이메일_없으면_400() throws Exception {
        String badJson = """
                {"courseType": "HARD_ONLY"}
                """;

        mockMvc.perform(post("/api/courses/enroll")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(badJson))
                .andExpect(status().isBadRequest());
    }

    @Test
    void GET_courses_me_today_오늘의_문제_목록_반환() throws Exception {
        given(courseService.getTodayQuestions("u@t.com")).willReturn(List.of(1L, 2L, 3L, 4L, 5L));

        mockMvc.perform(get("/api/courses/me/today").param("email", "u@t.com"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(5));
    }

    @Test
    void GET_courses_me_today_활성코스_없으면_404() throws Exception {
        given(courseService.getTodayQuestions("u@t.com"))
                .willThrow(new ResourceNotFoundException("활성 코스가 없습니다."));

        mockMvc.perform(get("/api/courses/me/today").param("email", "u@t.com"))
                .andExpect(status().isNotFound());
    }
}
