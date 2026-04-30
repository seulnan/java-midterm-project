package maeilmail.learning.domain.wrongnote;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import maeilmail.learning.common.exception.ResourceNotFoundException;
import java.util.List;
import java.util.Optional;
import maeilmail.learning.domain.wrongnote.dto.ReviewRequest;
import maeilmail.learning.domain.wrongnote.dto.WrongNoteDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

@ExtendWith(MockitoExtension.class)
class WrongNoteServiceTest {

    @Mock
    private WrongNoteRepository wrongNoteRepository;

    @InjectMocks
    private WrongNoteService wrongNoteService;

    @Test
    void registerOrSkip_새_오답_노트_저장() {
        given(wrongNoteRepository.findByUserEmailAndQuestionId("u@t.com", 1L))
                .willReturn(Optional.empty());

        wrongNoteService.registerOrSkip("u@t.com", 1L);

        verify(wrongNoteRepository).save(any(WrongNote.class));
    }

    @Test
    void registerOrSkip_이미_존재하면_저장_스킵() {
        WrongNote existing = WrongNote.create("u@t.com", 1L);
        given(wrongNoteRepository.findByUserEmailAndQuestionId("u@t.com", 1L))
                .willReturn(Optional.of(existing));

        wrongNoteService.registerOrSkip("u@t.com", 1L);

        verify(wrongNoteRepository, never()).save(any());
    }

    @Test
    void removeIfExists_존재하는_오답노트_삭제() {
        WrongNote note = WrongNote.create("u@t.com", 1L);
        given(wrongNoteRepository.findByUserEmailAndQuestionId("u@t.com", 1L))
                .willReturn(Optional.of(note));

        wrongNoteService.removeIfExists("u@t.com", 1L);

        verify(wrongNoteRepository).delete(note);
    }

    @Test
    void removeIfExists_존재하지_않으면_삭제_호출_없음() {
        given(wrongNoteRepository.findByUserEmailAndQuestionId("u@t.com", 99L))
                .willReturn(Optional.empty());

        wrongNoteService.removeIfExists("u@t.com", 99L);

        verify(wrongNoteRepository, never()).delete(any());
    }

    @Test
    void findDueNotes_복습_기한_도래한_노트만_반환() {
        WrongNote due = WrongNote.create("u@t.com", 5L);
        given(wrongNoteRepository.findByUserEmailAndNextReviewAtLessThanEqual(
                any(), any(LocalDateTime.class)))
                .willReturn(List.of(due));

        List<WrongNoteDto> result = wrongNoteService.findDueNotes("u@t.com");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).questionId()).isEqualTo(5L);
    }

    @Test
    void findMyNotes_페이지_반환() {
        WrongNote note = WrongNote.create("u@t.com", 3L);
        PageRequest pageable = PageRequest.of(0, 10);
        Page<WrongNote> page = new PageImpl<>(List.of(note));
        given(wrongNoteRepository.findByUserEmail("u@t.com", pageable)).willReturn(page);

        Page<WrongNoteDto> result = wrongNoteService.findMyNotes("u@t.com", pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).questionId()).isEqualTo(3L);
    }

    @Test
    void review_정답_처리_후_DTO_반환() {
        WrongNote note = WrongNote.create("u@t.com", 2L);
        given(wrongNoteRepository.findById(1L)).willReturn(Optional.of(note));

        WrongNoteDto result = wrongNoteService.review(1L, new ReviewRequest(true));

        assertThat(result.reviewCount()).isEqualTo(1);
    }

    @Test
    void review_존재하지_않는_ID면_예외() {
        given(wrongNoteRepository.findById(999L)).willReturn(Optional.empty());

        assertThatThrownBy(() -> wrongNoteService.review(999L, new ReviewRequest(true)))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("999");
    }
}
