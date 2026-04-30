# Answer 도메인 (domain-answer)

## 목표
Answer 엔티티 + Repository + Service + POST /api/answers REST API + 단위 테스트

## 영향 범위
- 새 파일 (모두 learning-api 내):
  - common/ApiResponse.java
  - common/ErrorCode.java
  - common/GlobalExceptionHandler.java
  - domain/answer/Answer.java
  - domain/answer/AnswerRepository.java
  - domain/answer/AnswerService.java
  - domain/answer/dto/SubmitAnswerRequest.java
  - domain/answer/dto/SubmitAnswerResponse.java
  - domain/answer/event/AnswerSubmittedEvent.java
  - api/AnswerController.java
  - src/test/java/.../domain/answer/AnswerServiceTest.java
- 수정 파일: 없음
- 영향 X 모듈: 기존 모듈 전부

## 단계 (체크리스트)

- [ ] 1. common/ApiResponse<T> record 작성
- [ ] 2. common/ErrorCode enum 작성
- [ ] 3. common/GlobalExceptionHandler @RestControllerAdvice 작성
- [ ] 4. domain/answer/Answer 엔티티 작성
  - @Entity, @Table(name="answers")
  - 필드: id, userEmail, questionId, submittedText, isCorrect, score, responseTimeMs, createdAt
  - createdAt: @CreatedDate + @Column(updatable=false)
  - @EntityListeners(AuditingEntityListener.class)
- [ ] 5. AnswerRepository (JpaRepository)
- [ ] 6. SubmitAnswerRequest record
- [ ] 7. SubmitAnswerResponse record
- [ ] 8. AnswerSubmittedEvent record (questionId, userEmail, isCorrect, responseTimeMs)
- [ ] 9. AnswerService 작성
  - submitAnswer(SubmitAnswerRequest) → SubmitAnswerResponse
  - 정답 판별: 단순화 (submittedText가 비어있지 않으면 isCorrect=true, score=100 — 실제 채점은 adapter slug에서)
  - AnswerSubmittedEvent 발행 (ApplicationEventPublisher)
- [ ] 10. AnswerController: POST /api/answers
- [ ] 11. AnswerServiceTest 작성 (Mockito)
- [ ] 12. 컴파일 + 테스트 실행

## 테스트 범위
- [ ] AnswerServiceTest: submitAnswer 정상 흐름, 이벤트 발행 검증

## 디자인 패턴
Observer (AnswerSubmittedEvent + Publisher — 리스너는 event-observer slug에서)

## 주의사항
- @EnableJpaAuditing은 LearningApiApplication에 이미 선언
- createdAt은 BaseEntity 없이 Answer에 직접 선언 (학습 범위 단순화)
- 정답 판별 로직은 단순화: isCorrect = !submittedText.isBlank(), score = isCorrect ? 100 : 0
  실제 채점(원본 Question.content 비교)은 adapter-original-domain slug에서 연결
