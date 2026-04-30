## 무엇을
Answer 도메인 구현 — 답안 제출 API, 이벤트 발행 기반 Observer 패턴, 공통 응답 래퍼

## 왜
학습 기능의 핵심 진입점은 "답안 제출"이다. 이 이벤트가 오답노트 등록, 통계 갱신, 메일 발송 등 후속 처리를 트리거한다. Observer 패턴(AnswerSubmittedEvent)으로 이 책임들을 분리해 AnswerService가 후속 처리를 직접 호출하지 않도록 한다.

## 어떻게
- `ApiResponse<T>` record로 모든 API 응답 일관화
- `Answer` 엔티티: `@EntityListeners(AuditingEntityListener.class)` + `@CreatedDate`로 감사 추적
- `AnswerService.submitAnswer()`: 저장 → `AnswerSubmittedEvent` 발행 (리스너는 event-observer slug에서 추가)
- 정답 판별: 현재는 `!submittedText.isBlank()` 단순화 (adapter-original-domain slug에서 실제 채점으로 교체)

## 고려했으나 채택하지 않은 대안

- **대안**: AnswerService에서 오답노트/통계 갱신을 직접 호출
  - **장점**: 코드 흐름이 한 곳에서 파악 가능
  - **기각 이유**: Service 간 강결합이 발생하며, 새 후속 처리 추가 시마다 AnswerService를 수정해야 한다. Observer 패턴으로 OCP를 지키고 각 책임을 독립 리스너에 위임한다.

- **대안**: 이벤트 대신 Kafka/RabbitMQ 메시지 큐 사용
  - **장점**: 서비스 간 완전한 비동기 분리, 실패 재처리 가능
  - **기각 이유**: 단일 JVM 내 처리로 충분한 학습 시연 환경에서 메시지 브로커는 과도한 인프라 비용. `@Async` + `ApplicationEventPublisher`로 목적 달성.

## 의도적으로 구현하지 않은 것

- 실제 정답 비교 로직 (Question.content와 submittedText 비교)
  - **이유**: 원본 mail-core의 Question 도메인과의 연결은 adapter-original-domain slug에서 분리 처리. 채점 로직 없이도 API 흐름과 이벤트 패턴을 시연할 수 있다.
  - **운영 시 검토 대상**: LegacyQuestionAdapter에서 Question.content 가져와 정규화 비교 (대소문자/공백 제거).

## 트레이드오프 요약
| 결정 | 얻은 것 | 잃은 것 |
|---|---|---|
| ApplicationEventPublisher | 리스너 추가/삭제가 AnswerService 무변경 | 트랜잭션 경계 주의 필요 (@TransactionalEventListener 고려) |
| 단순 채점 | 빠른 구현, 테스트 용이 | 실제 정답 검증 없음 |

## 변경 사항
- 추가: `common/ApiResponse`, `common/ErrorCode`, `common/GlobalExceptionHandler`
- 추가: `domain/answer/` (Answer, AnswerRepository, AnswerService, 이벤트, DTO)
- 추가: `api/AnswerController`
- 추가: `AnswerServiceTest` (3 테스트 케이스)
