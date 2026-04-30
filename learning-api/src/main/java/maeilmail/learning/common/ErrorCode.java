package maeilmail.learning.common;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ErrorCode {
    INVALID_REQUEST("잘못된 요청입니다."),
    NOT_FOUND("리소스를 찾을 수 없습니다."),
    ANSWER_NOT_FOUND("답안을 찾을 수 없습니다."),
    WRONG_NOTE_NOT_FOUND("오답노트를 찾을 수 없습니다."),
    USER_STAT_NOT_FOUND("사용자 통계를 찾을 수 없습니다."),
    COURSE_ENROLLMENT_NOT_FOUND("수강 정보를 찾을 수 없습니다."),
    DUPLICATE_COURSE_ENROLLMENT("이미 활성화된 코스가 있습니다."),
    INTERNAL_ERROR("내부 오류가 발생했습니다.");

    private final String message;
}
