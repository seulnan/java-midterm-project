package maeilmail.learning.api;

import lombok.RequiredArgsConstructor;
import maeilmail.learning.common.ApiResponse;
import maeilmail.learning.domain.userstat.UserStatService;
import maeilmail.learning.domain.userstat.dto.UserStatDto;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/stats")
@RequiredArgsConstructor
public class UserStatController {

    private final UserStatService userStatService;

    @GetMapping("/me")
    public ApiResponse<UserStatDto> getMyStat(@RequestParam String email) {
        return ApiResponse.ok(userStatService.findByEmail(email));
    }
}
