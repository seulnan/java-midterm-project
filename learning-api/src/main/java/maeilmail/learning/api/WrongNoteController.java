package maeilmail.learning.api;

import java.util.List;
import lombok.RequiredArgsConstructor;
import maeilmail.learning.common.ApiResponse;
import maeilmail.learning.domain.wrongnote.WrongNoteService;
import maeilmail.learning.domain.wrongnote.dto.ReviewRequest;
import maeilmail.learning.domain.wrongnote.dto.WrongNoteDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/wrong-notes")
@RequiredArgsConstructor
public class WrongNoteController {

    private final WrongNoteService wrongNoteService;

    @GetMapping("/me")
    public ApiResponse<Page<WrongNoteDto>> getMyNotes(
            @RequestParam String email,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ApiResponse.ok(wrongNoteService.findMyNotes(email, PageRequest.of(page, size)));
    }

    @GetMapping("/me/due")
    public ApiResponse<List<WrongNoteDto>> getDueNotes(@RequestParam String email) {
        return ApiResponse.ok(wrongNoteService.findDueNotes(email));
    }

    @PostMapping("/{id}/review")
    public ApiResponse<WrongNoteDto> review(
            @PathVariable Long id,
            @RequestBody ReviewRequest request) {
        return ApiResponse.ok(wrongNoteService.review(id, request));
    }
}
