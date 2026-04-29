## 무엇을
Adapter 패턴으로 메일 발송 추상화 — LearningMailSender 인터페이스 + MockMailSender/SmtpMailSender 구현, 복습 메일 스케줄러

## 왜
메일 발송 구현체가 코드 전체에 직접 참조되면 local/dev/prod 환경 전환 시 코드를 바꿔야 한다. Adapter 패턴으로 인터페이스만 참조하게 하면 Spring Profile 하나로 구현체를 교체할 수 있다. 또한 MailNotificationListener가 이제 실제 LearningMailSender를 주입받아 오답 발생 시 메일을 발송한다.

## 어떻게
- `LearningMailSender` (인터페이스): `send(to, subject, body)` 단일 메서드
- `MockMailSender`: local/test 프로파일 — 콘솔 로그 + 인메모리 `sentLogs` (테스트 검증용)
- `SmtpMailSender`: dev 프로파일 — `JavaMailSender` 위임, Gmail SMTP 실제 발송
- `MailConfig`: `@Profile` 기반 빈 선택 — 소스 수정 없이 환경 전환
- `ReviewMailScheduler`: `@Scheduled(cron="0 0 6 * * *")` + `@Async("mailExecutor")` — 매일 06:00 KST 복습 알림
- `DevController`: `/api/dev/test-mail` — local/dev에서 즉시 발송 확인

## 고려했으나 채택하지 않은 대안

- **대안**: 원본 mail-core의 `AbstractMailSender` 상속
  - **장점**: 재시도 로직, 이벤트 기록 등 원본 기능 재사용
  - **기각 이유**: learning-api는 mail-core를 의존하지 않는 독립 모듈. 원본의 Bucket4j, MySQL 이벤트 저장소까지 끌려오는 비용이 크다.

- **대안**: `spring.mail.sender` 프로파일 대신 `@ConditionalOnProperty`
  - **장점**: yml 한 줄로 구현체 전환 가능
  - **기각 이유**: `@Profile`이 Spring 표준이며 배포 환경과 1:1 대응이 더 명확. `@ConditionalOnProperty`는 기능 플래그 용도에 적합.

## 의도적으로 구현하지 않은 것

- 메일 발송 실패 재처리 (DLQ/재시도)
  - **이유**: 학습 시연 범위. 실패 시 로그만 남김.
  - **운영 시 검토 대상**: `spring-retry` + `@Retryable` 적용 (원본 mail-core 패턴 참조), 또는 RabbitMQ DLQ 도입.

## 트레이드오프 요약
| 결정 | 얻은 것 | 잃은 것 |
|---|---|---|
| 인터페이스 추상화 | 환경 전환 무코드 | 인터페이스+구현체 파일 추가 |
| MockMailSender 인메모리 로그 | 테스트에서 발송 여부 검증 가능 | 멀티스레드 환경에서 ArrayList 비안전 (테스트 전용) |

## 변경 사항
- 추가: `infrastructure/mail/LearningMailSender` (인터페이스)
- 추가: `infrastructure/mail/MockMailSender`, `SmtpMailSender`
- 추가: `infrastructure/mail/ReviewMailScheduler`
- 추가: `config/MailConfig` (프로파일별 빈 선택)
- 수정: `event/listener/MailNotificationListener` (LearningMailSender 주입)
- 추가: `api/DevController` (/api/dev/test-mail)
- 수정: `learning-api/build.gradle` — spring-boot-starter-mail 추가
- 추가: `MockMailSenderTest` (3 테스트 케이스)
