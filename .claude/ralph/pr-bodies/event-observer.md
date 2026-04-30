## 무엇을
Observer 패턴 구현 — AnswerSubmittedEvent에 3개 리스너 연결 (오답노트/통계/메일 큐잉)

## 왜
답안 제출이라는 단일 이벤트에 오답노트 등록, 통계 갱신, 메일 알림이라는 3가지 부수 효과가 연결된다. AnswerService에서 이 셋을 직접 호출하면 기능 추가마다 Service를 수정해야 하고 테스트 격리도 어렵다. Observer 패턴으로 이벤트 발행자(AnswerService)와 처리자(리스너)를 분리해 OCP를 지킨다.

## 어떻게
- `@TransactionalEventListener(phase = AFTER_COMMIT)`: 답안 저장 트랜잭션 커밋 후 실행 — 트랜잭션 롤백 시 오답노트/통계에 영향 없음
- `WrongNoteRegistrationListener`: 오답이면 등록, 정답이면 제거 (동기, AFTER_COMMIT)
- `UserStatUpdateListener`: `@Async` — 통계 갱신이 답안 응답을 블로킹하지 않음
- `MailNotificationListener`: `@Async("mailExecutor")` — 전용 스레드풀로 메일 큐잉
- `AsyncConfig`: `mailExecutor` ThreadPoolTaskExecutor 빈 등록 (coreSize=3, max=10)

## 고려했으나 채택하지 않은 대안

- **대안**: `@EventListener` (일반) 대신 `@TransactionalEventListener` 사용
  - **장점**: 즉시 처리, 트랜잭션과 무관
  - **기각 이유**: `@EventListener`는 발행자의 트랜잭션 안에서 실행되므로 리스너 실패가 답안 저장 롤백을 유발할 수 있다. `AFTER_COMMIT`으로 분리해야 부수 효과 실패가 핵심 흐름에 영향을 주지 않는다.

- **대안**: Kafka 토픽으로 이벤트 발행
  - **장점**: 서비스 간 완전 분리, 재처리 보장
  - **기각 이유**: 단일 JVM 학습 환경에서 Kafka 인프라 도입은 과도. Spring ApplicationEvent로 동일한 패턴 교육 목적 달성.

## 의도적으로 구현하지 않은 것

- MailNotificationListener의 실제 메일 발송 로직
  - **이유**: 메일 발송은 LearningMailSender 인터페이스 구현(mail-strategy slug)에 의존. 현재는 로그만 남기고 자리를 마련.
  - **운영 시 검토 대상**: 이벤트 처리 실패 시 Dead Letter Queue 도입, 최소 1회 발송 보장.

## 트레이드오프 요약
| 결정 | 얻은 것 | 잃은 것 |
|---|---|---|
| AFTER_COMMIT | 답안 롤백 시 오답노트 오염 방지 | 리스너 실패 재처리 없음 |
| @Async 통계 갱신 | 답안 응답 시간 단축 | 즉시 조회 시 통계 반영 지연 가능 |

## 변경 사항
- 추가: `config/AsyncConfig` (mailExecutor 빈)
- 추가: `event/listener/` 3개 리스너
- 추가: `WrongNoteRegistrationListenerTest` (2 테스트 케이스)
