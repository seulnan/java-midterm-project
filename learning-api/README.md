# 매일메일 Plus — learning-api

maeil-mail 포크 위에 구축한 **적응형 학습 백엔드** 모듈.

---

## 목차

1. [모듈 개요](#1-모듈-개요)
2. [아키텍처 & 디자인 패턴](#2-아키텍처--디자인-패턴)
3. [도메인 모델](#3-도메인-모델)
4. [REST API](#4-rest-api)
5. [환경 설정 & 실행](#5-환경-설정--실행)
6. [동시성 전략](#6-동시성-전략)
7. [이벤트 플로우](#7-이벤트-플로우)
8. [테스트](#8-테스트)

---

## 1. 모듈 개요

| 항목 | 값 |
|------|-----|
| 포트 | `8081` |
| 베이스 패키지 | `maeilmail.learning` |
| 빌드 타입 | `java-boot-mvc-application` |
| 데이터베이스 | PostgreSQL (Supabase, dev) / H2 (test) |
| Java | 17 |
| Spring Boot | 3.5.3 |

learning-api는 원본 mail-api(포트 8080)와 **독립된** Spring Boot 애플리케이션이다.  
원본 모듈(`mail-core`, `mail-api` 등)은 절대 수정하지 않는다.

---

## 2. 아키텍처 & 디자인 패턴

### 2-1. 패턴 맵

```
┌────────────────────────────────────────────────────────────┐
│                        learning-api                        │
│                                                            │
│  [POST /api/answers]                                       │
│        │                                                   │
│        ▼                                                   │
│  AnswerService ──publish──► AnswerSubmittedEvent           │
│        │                           │                       │
│        │              ┌────────────┼────────────┐          │
│        │              ▼            ▼            ▼          │
│        │  WrongNoteRegistration UserStatUpdate MailNotif   │
│        │  Listener(AFTER_COMMIT) Listener      Listener    │
│        │                                    (@Async)       │
│        ▼                                                   │
│  QuestionRecommenderFactory.create(difficulty)             │
│        │                                                   │
│        ├─► EasyRecommender  (ID 1~50)                      │
│        ├─► MediumRecommender(ID 51~100)                    │
│        └─► HardRecommender  (ID 101~200)                   │
│                                                            │
│  CourseService.getTodayQuestions()                         │
│        └─► Map<CourseType, CoursePolicy> 전략 디스패치     │
│               ├─► ShortIntensivePolicy                     │
│               ├─► HardOnlyPolicy                          │
│               └─► WeaknessFocusedPolicy                   │
│                                                            │
│  LegacyQuestionPort (Adapter)                              │
│        └─► LegacyQuestionAdapter (mail-core 번역 계층)     │
└────────────────────────────────────────────────────────────┘
```

### 2-2. GoF 패턴 매핑

| 패턴 | 위치 | 역할 |
|------|------|------|
| **Adapter** | `infrastructure/mail/LearningMailSender` | 환경별 메일 발송 방식 교체 (SMTP ↔ Mock) |
| **Adapter** | `adapter/LegacyQuestionPort` + `LegacyQuestionAdapter` | mail-core Question → learning-api 도메인 번역 |
| **Strategy** | `domain/course/policy/CoursePolicy` + 3 구현 | 코스 타입별 문제 선택 로직 교체 |
| **Observer** | `AnswerSubmittedEvent` + 3 리스너 | 답안 제출 부수 효과 분리 |
| **Factory** | `infrastructure/recommender/QuestionRecommenderFactory` | 난이도 → 추천기 인스턴스 매핑 |

---

## 3. 도메인 모델

### Answer (답안)
- 사용자가 문제에 제출한 답안 기록
- `questionId`: mail-core의 Question ID 참조 (FK 없는 소프트 참조)
- `score`: 0~100점, `responseTimeMs`: 응답 시간 (ms)

### WrongNote (오답 노트)
- SM-2 간격 반복 알고리즘 적용
- `intervalDays`: 다음 복습까지 남은 일수 (최소 1)
- `easeFactor`: 복습 난이도 가중치 (최소 1.3, 기본 2.5)
- `nextReviewAt`: 다음 복습 예정일

**SM-2 규칙:**

| 정오 | interval | easeFactor |
|------|----------|------------|
| 정답 | `round(interval × ease)` | `ease + 0.1` |
| 오답 | `1` (리셋) | `max(1.3, ease - 0.2)` |

### UserStat (학습 통계)
- 사용자별 누적 통계 (정답 수, 오답 수, 현재 난이도)
- `currentDifficulty`: 최근 20문제 정답률로 자동 조정
  - `> 80%` → 난이도 상승, `< 40%` → 난이도 하강

### CourseEnrollment (코스 수강)
- `CourseType`: `SHORT_INTENSIVE` / `HARD_ONLY` / `WEAKNESS_FOCUSED`
- 동시 1개 활성 코스만 허용

---

## 4. REST API

> Base URL: `http://localhost:8081`

### 답안 제출
```
POST /api/answers
Content-Type: application/json

{
  "userEmail": "user@example.com",
  "questionId": 1,
  "submittedText": "REST는 ...",
  "isCorrect": true,
  "score": 85,
  "responseTimeMs": 12000
}
```

### 오답 노트
```
GET  /api/wrong-notes/me?email=user@example.com&page=0&size=20
GET  /api/wrong-notes/me/due?email=user@example.com
POST /api/wrong-notes/{id}/review
     { "isCorrect": true }
```

### 학습 통계
```
GET /api/stats/me?email=user@example.com
```

### 코스 관리
```
POST /api/courses/enroll
     { "userEmail": "...", "courseType": "SHORT_INTENSIVE" }

GET  /api/courses/me/today?email=user@example.com
```

### 개발용 (local / dev)
```
POST /api/dev/test-mail
     { "to": "...", "subject": "테스트" }
```

### 헬스 체크
```
GET /actuator/health
```

---

## 5. 환경 설정 & 실행

### 로컬 실행

```bash
# 1. 루트 디렉토리에서
./gradlew :learning-api:bootRun --args='--spring.profiles.active=local'
```

`local` 프로파일: H2 인메모리 DB, MockMailSender(콘솔 로그), 8081 포트

### dev 환경 (Supabase)

`.env` 파일에 다음 변수 필요 (`.gitignore`에 등록됨):

```
SUPABASE_URL=jdbc:postgresql://...
SUPABASE_USERNAME=postgres
SUPABASE_PASSWORD=...
MAIL_USERNAME=your@gmail.com
MAIL_PASSWORD=your-app-password
```

```bash
./gradlew :learning-api:bootRun --args='--spring.profiles.active=dev'
```

### 빌드만

```bash
./gradlew :learning-api:build
```

---

## 6. 동시성 전략

`UserStat.recordAnswer()`는 두 가지 보호 계층을 동시에 적용한다.

```java
// 1. 메서드 수준 synchronized — 단일 JVM 내 스레드 순서 보장
public synchronized void recordAnswer(boolean isCorrect) { ... }

// 2. @Version (낙관적 락) — DB 레벨 충돌 감지
@Version
private Long version;
```

**Race Condition 시연 테스트** (`UserStatRaceConditionTest`):

| 구현 | 100 스레드 × 1,000 증가 | 결과 |
|------|------------------------|------|
| `UnsafeCounter` (보호 없음) | 손실 발생 | `< 100,000` |
| `SafeCounter` (`synchronized`) | 정확 | `= 100,000` |

---

## 7. 이벤트 플로우

답안 제출 후 `@TransactionalEventListener(AFTER_COMMIT)`으로 부수 효과 처리:

```
AnswerService.submitAnswer()
  └─ save(answer)
  └─ publish(AnswerSubmittedEvent)
       │
       ├─ [AFTER_COMMIT] WrongNoteRegistrationListener
       │     정답 → 오답노트 삭제
       │     오답 → 오답노트 등록 (중복 시 skip)
       │
       ├─ [AFTER_COMMIT] UserStatUpdateListener
       │     UserStat.recordAnswer() 호출 → 정답률 갱신 → 난이도 자동 조정
       │
       └─ [AFTER_COMMIT + @Async("mailExecutor")] MailNotificationListener
             오답 발생 시 복습 알림 메일 큐잉 (별도 스레드풀)
```

---

## 8. 테스트

```bash
# 전체 학습 모듈 테스트
./gradlew :learning-api:test

# 도메인별 테스트
./gradlew :learning-api:test --tests "maeilmail.learning.domain.answer.*"
./gradlew :learning-api:test --tests "maeilmail.learning.domain.wrongnote.*"
./gradlew :learning-api:test --tests "maeilmail.learning.domain.userstat.*"
./gradlew :learning-api:test --tests "maeilmail.learning.event.*"
./gradlew :learning-api:test --tests "maeilmail.learning.infrastructure.recommender.*"
./gradlew :learning-api:test --tests "maeilmail.learning.adapter.*"
```

### 주요 테스트 목록

| 테스트 클래스 | 검증 내용 |
|-------------|----------|
| `AnswerServiceTest` | 답안 저장 + 이벤트 발행 |
| `WrongNoteTest` | SM-2 경계값 (easeFactor 최솟값 1.3) |
| `UserStatRaceConditionTest` | 100스레드 동시 갱신 정확성 |
| `WrongNoteRegistrationListenerTest` | AFTER_COMMIT 이벤트 처리 |
| `MockMailSenderTest` | 로컬 메일 발송 기록 |
| `QuestionRecommenderFactoryTest` | 난이도별 추천기 범위 검증 |
| `LegacyQuestionAdapterTest` | Adapter 패턴 동작 검증 |

---

> 이 모듈은 [maeil-mail](https://github.com/maeil-mail/maeil-mail-be) 포크 위에서 동작하며,  
> 원본 코드는 수정하지 않습니다.
