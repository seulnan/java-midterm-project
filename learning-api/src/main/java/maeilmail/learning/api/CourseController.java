package maeilmail.learning.api;

import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.learning.common.ApiResponse;
import maeilmail.learning.domain.course.CourseService;
import maeilmail.learning.domain.course.dto.CourseEnrollmentDto;
import maeilmail.learning.domain.course.dto.EnrollRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/courses")
@RequiredArgsConstructor
public class CourseController {

    private final CourseService courseService;

    @PostMapping("/enroll")
    public ApiResponse<CourseEnrollmentDto> enroll(@Valid @RequestBody EnrollRequest request) {
        return ApiResponse.ok(courseService.enroll(request));
    }

    @GetMapping("/me/today")
    public ApiResponse<List<Long>> getTodayQuestions(@RequestParam String email) {
        return ApiResponse.ok(courseService.getTodayQuestions(email));
    }
}
