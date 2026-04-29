## 무엇을
WrongNote 도메인 구현 — SM-2 기반 간격 반복 복습 알고리즘, 오답노트 CRUD API

## 왜
반복 노출이 없는 단순 오답 목록은 학습 효과가 제한적이다. SM-2 알고리즘은 기억 곡선을 기반으로 "잊혀지기 직전에 복습"하는 최적 주기를 산출한다. ease_factor로 개인별 학습 속도를 자동 조절해 난이도 적응이 이루어진다.

## 어떻게
- `WrongNote.applyReview(isCorrect)`: 도메인 메서드로 SM-2 로직 캡슐화
  - 정답: `interval = round(interval × ease)`, `ease += 0.1`
  - 오답: `interval = 1`, `ease = max(1.3, ease - 0.2)`
- `WrongNoteRepository.findByUserEmailAndNextReviewAtLessThanEqual`: 오늘 복습 대상 쿼리
- `WrongNoteService.registerOrSkip`: 이미 존재하면 중복 등록 방지 (event-observer slug에서 이벤트 리스너가 호출)

## 고려했으나 채택하지 않은 대안

- **대안**: SM-2를 별도 Sm2Algorithm 클래스로 분리
  - **장점**: 알고리즘 테스트가 독립적으로 가능, 교체 용이
  - **기각 이유**: 현재 WrongNote가 알고리즘의 유일한 사용처. 분리해도 클래스가 늘어나는 비용 대비 이득이 없다. 추후 알고리즘 교체 요구가 생기면 분리한다.

- **대안**: next_review_at을 클라이언트가 결정
  - **장점**: 서버 로직 단순화
  - **기각 이유**: 복습 주기 산출은 서버 책임이어야 한다. 클라이언트가 조작할 경우 알고리즘 효과가 없어진다.

## 의도적으로 구현하지 않은 것

- 오답노트 삭제 API (연속 정답 시 자동 제거)
  - **이유**: 삭제 트리거는 AnswerSubmittedEvent를 처리하는 이벤트 리스너(event-observer slug)에서 담당. 현재는 `removeIfExists()` 서비스 메서드만 제공.
  - **운영 시 검토 대상**: 정답 N회 연속 시 삭제 vs 영구 보관 정책 결정.

## 트레이드오프 요약
| 결정 | 얻은 것 | 잃은 것 |
|---|---|---|
| 도메인 메서드(applyReview)에 SM-2 로직 포함 | 엔티티 불변 캡슐화, 테스트 용이 | 알고리즘 교체 시 엔티티 수정 필요 |
| unique constraint (user_email, question_id) | DB 레벨 중복 방지 | 동시 삽입 시 DataIntegrityViolationException 처리 필요 |

## 변경 사항
- 추가: `domain/wrongnote/` (WrongNote, WrongNoteRepository, WrongNoteService, DTO)
- 추가: `api/WrongNoteController` (목록/오늘 복습/복습 완료 3개 엔드포인트)
- 추가: `WrongNoteTest` (SM-2 경계값 5개 테스트 케이스)
