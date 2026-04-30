package maeilmail.learning.domain.userstat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;
import maeilmail.learning.common.exception.ResourceNotFoundException;
import maeilmail.learning.domain.userstat.dto.UserStatDto;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class UserStatServiceTest {

    @Mock
    private UserStatRepository userStatRepository;

    @InjectMocks
    private UserStatService userStatService;

    @Test
    void recordAnswer_기존_사용자_갱신() {
        UserStat stat = UserStat.create("u@t.com");
        given(userStatRepository.findByUserEmailForUpdate("u@t.com")).willReturn(Optional.of(stat));

        userStatService.recordAnswer("u@t.com", true, 1500L);

        assertThat(stat.getTotalAttempts()).isEqualTo(1);
        assertThat(stat.getCorrectCount()).isEqualTo(1);
    }

    @Test
    void recordAnswer_신규_사용자_생성_후_갱신() {
        UserStat newStat = UserStat.create("new@t.com");
        given(userStatRepository.findByUserEmailForUpdate("new@t.com")).willReturn(Optional.empty());
        given(userStatRepository.saveAndFlush(any())).willReturn(newStat);

        userStatService.recordAnswer("new@t.com", false, 2000L);

        verify(userStatRepository).saveAndFlush(any(UserStat.class));
        assertThat(newStat.getTotalAttempts()).isEqualTo(1);
    }

    @Test
    void findByEmail_존재하는_사용자_반환() {
        UserStat stat = UserStat.create("u@t.com");
        given(userStatRepository.findByUserEmail("u@t.com")).willReturn(Optional.of(stat));

        UserStatDto dto = userStatService.findByEmail("u@t.com");

        assertThat(dto.userEmail()).isEqualTo("u@t.com");
        assertThat(dto.currentDifficulty()).isEqualTo(Difficulty.EASY);
    }

    @Test
    void findByEmail_존재하지_않으면_예외() {
        given(userStatRepository.findByUserEmail("ghost@t.com")).willReturn(Optional.empty());

        assertThatThrownBy(() -> userStatService.findByEmail("ghost@t.com"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("ghost@t.com");
    }
}
