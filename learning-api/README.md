# 매일메일 Plus — 적응형 학습 백엔드 (learning-api)

## 프로그램 개요

[매일메일](https://github.com/maeil-mail/maeil-mail-be) 오픈소스 프로젝트를 클론하여, 기술 면접 구독 서비스 위에  
**개인 맞춤형 학습 기능**을 추가한 Spring Boot 백엔드 모듈입니다.

사용자가 문제를 풀면 오답 노트가 자동 생성되고, SM-2 간격 반복 알고리즘으로 복습 주기를 계산합니다.  
학습 통계를 바탕으로 난이도가 자동 조정되며, 복습 시점이 되면 메일로 알림을 보냅니다.

- 포트: **8081** (원본 mail-app 8080과 독립)
- 원본 코드는 **일절 수정하지 않고** 새 모듈(`learning-api`)만 추가

---

## 원본 저장소

> 클론 후 `learning-api` 모듈 추가 개발  
> **원본 URL**: https://github.com/maeil-mail/maeil-mail-be

---

## 사용한 주요 자바 개념

### 객체지향 심화
| 개념 | 적용 위치 |
|------|----------|
| 인터페이스 분리 원칙 | `LearningMailSender`, `LegacyQuestionPort`, `CoursePolicy`, `QuestionRecommender` — 구현체와 호출자 완전 분리 |
| 다형성 | Spring이 `List<CoursePolicy>` 주입 → 런타임에 올바른 구현체 선택 |
| 캡슐화 | `WrongNote.applyReview()` — SM-2 알고리즘 내부 상태를 엔티티 안에 은닉 |
| 이벤트 기반 설계 | `AnswerSubmittedEvent` — 답안 제출과 부수 효과(오답 노트/통계/메일)를 완전 분리 |

### 디자인 패턴 (GoF 4종)
| 패턴 | 구현 클래스 | 핵심 |
|------|------------|------|
| **Adapter** | `LearningMailSender` + `MockMailSender` / `SmtpMailSender` | `@Profile`로 환경마다 구현체 교체 |
| **Adapter** | `LegacyQuestionPort` + `LegacyQuestionAdapter` | mail-core `Question` ↔ learning-api 도메인 번역 계층 |
| **Strategy** | `CoursePolicy` + `ShortIntensivePolicy` / `HardOnlyPolicy` / `WeaknessFocusedPolicy` | 코스 타입별 문제 선택 로직 런타임 교체 |
| **Observer** | `AnswerSubmittedEvent` + 3개 `@TransactionalEventListener` | DB 커밋 이후에만 부수 효과 실행 |
| **Factory** | `QuestionRecommenderFactory.create(Difficulty)` | 난이도 → 추천기 인스턴스 동적 생성 |

### 제네릭 / 컬렉션
```java
// Factory: List<T> 주입 → Map<K,V> 변환 (타입 안전 Factory)
private final List<QuestionRecommender> recommenders;

Map<Difficulty, QuestionRecommender> map = recommenders.stream()
    .collect(Collectors.toMap(QuestionRecommender::difficulty, Function.identity()));

// Strategy: Map<CourseType, CoursePolicy> 런타임 디스패치
Map<CourseType, CoursePolicy> policyMap = policies.stream()
    .collect(Collectors.toMap(CoursePolicy::courseType, Function.identity()));
```

### 람다 / 스트림 API
```java
// LegacyQuestionAdapter: 스트림 + 메서드 레퍼런스 + filter + collect
List<LegacyQuestion> result = store.values().stream()
    .filter(q -> q.category().equalsIgnoreCase(category))
    .collect(Collectors.toList());

// in-memory store 구성: LongStream + mapToObj + Collectors.toMap
LongStream.rangeClosed(1, 200)
    .mapToObj(id -> new LegacyQuestion(id, "Question #" + id, ...))
    .collect(Collectors.toMap(LegacyQuestion::id, q -> q));
```

### 멀티스레드 기초
```java
// UserStat: synchronized 메서드 + @Version 낙관적 락 이중 보호
@Version
private Long version;  // JPA 낙관적 락 — DB 레벨 충돌 감지

public synchronized void recordAnswer(boolean isCorrect) { ... }

// 비동기 메일 발송: ThreadPoolTaskExecutor + @Async
@Async("mailExecutor")
@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
public void onAnswerSubmitted(AnswerSubmittedEvent event) { ... }
```

**Race Condition 시연 테스트** (`UserStatRaceConditionTest`):
- `UnsafeCounter`: 100 스레드 × 1,000 증가 → 손실 발생 (`< 100,000`)
- `SafeCounter`: `synchronized` 적용 → 정확히 `100,000`

---

## 클래스 구성 설명

```
maeilmail.learning
├── LearningApiApplication            메인 클래스 (@SpringBootApplication)
│
├── config/
│   ├── AsyncConfig                   ThreadPoolTaskExecutor("mailExecutor") 정의
│   ├── MailConfig                    @Profile 기반 LearningMailSender 빈 선택
│   └── SchedulerConfig               @EnableScheduling
│
├── common/
│   ├── ApiResponse<T>                제네릭 공통 응답 래퍼 record
│   ├── GlobalExceptionHandler        @RestControllerAdvice
│   └── ErrorCode                     에러 코드 enum
│
├── domain/
│   ├── answer/
│   │   ├── Answer                    @Entity — 답안 기록
│   │   ├── AnswerRepository          Spring Data JPA
│   │   ├── AnswerService             submitAnswer() → event publish
│   │   └── event/AnswerSubmittedEvent  도메인 이벤트
│   │
│   ├── wrongnote/
│   │   ├── WrongNote                 @Entity — SM-2 상태 보유 (interval, easeFactor)
│   │   ├── WrongNoteRepository
│   │   └── WrongNoteService          오답 등록 / 복습 처리
│   │
│   ├── userstat/
│   │   ├── UserStat                  @Entity — @Version 낙관적 락 + synchronized 갱신
│   │   ├── UserStatRepository
│   │   └── UserStatService           findOrCreate + recordAnswer (동시성 보호)
│   │
│   └── course/
│       ├── CourseEnrollment          @Entity — 수강 정보
│       ├── CourseService             getTodayQuestions() — Strategy 디스패치
│       └── policy/
│           ├── CoursePolicy          인터페이스 (Strategy Target)
│           ├── ShortIntensivePolicy  7일 집중 코스
│           ├── HardOnlyPolicy        HARD 문제만
│           └── WeaknessFocusedPolicy 취약 카테고리 집중
│
├── infrastructure/
│   ├── mail/
│   │   ├── LearningMailSender        인터페이스 (Adapter Target)
│   │   ├── SmtpMailSender            Gmail SMTP (@Profile("dev"))
│   │   └── MockMailSender            콘솔 로그 / 인메모리 (@Profile("local","test"))
│   │
│   └── recommender/
│       ├── QuestionRecommender       인터페이스 (Strategy + Factory)
│       ├── EasyRecommender           ID 1~50
│       ├── MediumRecommender         ID 51~100
│       ├── HardRecommender           ID 101~200
│       └── QuestionRecommenderFactory  create(Difficulty) → 구현체 반환
│
├── adapter/
│   ├── LegacyQuestion                record DTO
│   ├── LegacyQuestionPort            인터페이스 (Adapter Target)
│   └── LegacyQuestionAdapter         mail-core Question 번역 구현체
│
├── api/
│   ├── AnswerController              POST /api/answers
│   ├── WrongNoteController           GET/POST /api/wrong-notes
│   ├── UserStatController            GET /api/stats/me
│   ├── CourseController              POST /api/courses/enroll, GET /api/courses/me/today
│   └── DevController                 POST /api/dev/test-mail (@Profile local/dev)
│
└── event/listener/
    ├── WrongNoteRegistrationListener @TransactionalEventListener(AFTER_COMMIT) — 오답 노트 등록/삭제
    ├── UserStatUpdateListener        @TransactionalEventListener(AFTER_COMMIT) — 통계 갱신
    └── MailNotificationListener      @Async + @TransactionalEventListener(AFTER_COMMIT) — 메일 큐잉
```

---

## 실행 방법

### 로컬 실행 (H2 인메모리 DB)

```bash
# 1. 저장소 클론
git clone https://github.com/seulnan/java-midterm-project.git
cd java-midterm-project

# 2. 로컬 프로파일로 실행 (MockMailSender, H2)
./gradlew :learning-api:bootRun --args='--spring.profiles.active=local'

# 3. 헬스 체크
curl http://localhost:8081/actuator/health
```

### 테스트 실행

```bash
# 전체 테스트 (118개)
./gradlew :learning-api:test

# 계층별 실행
./gradlew :learning-api:test --tests "maeilmail.learning.domain.*"
./gradlew :learning-api:test --tests "maeilmail.learning.api.*"
./gradlew :learning-api:test --tests "maeilmail.learning.integration.*"
./gradlew :learning-api:test --tests "maeilmail.learning.event.*"
./gradlew :learning-api:test --tests "maeilmail.learning.infrastructure.*"
./gradlew :learning-api:test --tests "maeilmail.learning.adapter.*"
```

**요구 사항**: Java 17+, Gradle 8.x

---

## 테스트 설계 원칙

### 계층 분리 (Testing Pyramid)

```
          ┌─────────────┐
          │  E2E / 통합  │  6개  @SpringBootTest — 실제 Spring 컨텍스트, 전체 흐름
          ├─────────────┤
          │  Controller │ 15개  @WebMvcTest — HTTP 계층, 상태 코드, 직렬화
          ├─────────────┤
          │  Repository │ 22개  @DataJpaTest — 실제 SQL, 제약 조건, 쿼리
          ├─────────────┤
          │   Service   │ 30개  Mockito — 비즈니스 로직 격리
          ├─────────────┤
          │   Domain    │ 45개  순수 Java — 엔티티·알고리즘·열거형
          └─────────────┘
```

| 계층 | 도구 | Spring 컨텍스트 | 속도 |
|------|------|----------------|------|
| 엔티티 / 알고리즘 | JUnit 5 + AssertJ | 없음 | 즉시 |
| 서비스 | Mockito `@ExtendWith` | 없음 | 빠름 |
| Repository | `@DataJpaTest` + H2 | JPA 슬라이스 | 중간 |
| Controller | `@WebMvcTest` + MockMvc | MVC 슬라이스 | 중간 |
| E2E | `@SpringBootTest(NONE)` + H2 | 전체 | 느림 |

### 원칙 1 — 계층별 책임 격리

```java
// 서비스 테스트: Repository는 Mock → 비즈니스 로직만 검증
@ExtendWith(MockitoExtension.class)
class WrongNoteServiceTest {
    @Mock WrongNoteRepository wrongNoteRepository;
    @InjectMocks WrongNoteService wrongNoteService;

    @Test
    void registerOrSkip_이미_존재하면_저장_스킵() {
        given(wrongNoteRepository.findByUserEmailAndQuestionId(...))
            .willReturn(Optional.of(existing));
        wrongNoteService.registerOrSkip("u@t.com", 1L);
        verify(wrongNoteRepository, never()).save(any()); // save 미호출 검증
    }
}
```

```java
// Repository 테스트: 실제 H2 DB → SQL·제약 조건 검증
@DataJpaTest
@EnableJpaAuditing
class WrongNoteRepositoryTest {
    @Test
    void 동일_user_question_중복_저장_시_예외() {
        repository.saveAndFlush(WrongNote.create("user@t.com", 1L));
        assertThatThrownBy(() -> repository.saveAndFlush(WrongNote.create("user@t.com", 1L)))
            .isInstanceOf(DataIntegrityViolationException.class); // UniqueConstraint 실제 검증
    }
}
```

### 원칙 2 — 경계값 우선 테스트

SM-2 알고리즘의 `easeFactor` 하한(1.3), 난이도 조정 임계값(80%/40%),  
20문제 미만에서의 조정 억제 등 **코드에서 가장 버그가 나기 쉬운 경계**를 집중 검증합니다.

```java
@Test
void easeFactor는_1_3_아래로_떨어지지_않는다() {
    WrongNote note = WrongNote.create("user@test.com", 1L);
    for (int i = 0; i < 20; i++) note.applyReview(false); // 20번 연속 오답
    assertThat(note.getEaseFactor()).isGreaterThanOrEqualTo(1.3); // 하한 고정
}

@ParameterizedTest
@CsvSource({"20,true", "25,true", "21,false", "22,false"})
void 배수5_시도에서만_난이도_평가(int attempts, boolean shouldAdjust) { ... }
```

### 원칙 3 — 행동 기반 검증 (BDD 스타일)

Mockito `given/when/verify` 패턴으로 **호출 여부와 횟수**까지 검증합니다.

```java
// "정답이면 오답노트를 삭제해야 한다"
@Test
void 정답_이벤트_수신_시_오답노트_제거() {
    AnswerSubmittedEvent event = new AnswerSubmittedEvent(2L, "user@test.com", 10L, true, 800L);
    listener.handle(event);
    verify(wrongNoteService).removeIfExists("user@test.com", 10L); // 정확한 메서드 호출 검증
}
```

### 원칙 4 — HTTP 계층 명세 검증 (`@WebMvcTest`)

컨트롤러 테스트는 **HTTP 상태 코드, 응답 JSON 구조, 검증 에러**를 검증합니다.  
서비스는 Mock 처리하여 HTTP 계층에만 집중합니다.

```java
@Test
void POST_answers_submittedText_빈_문자열_400() throws Exception {
    String badJson = """{"questionId": 1, "submittedText": "", ...}""";
    mockMvc.perform(post("/api/answers")
            .contentType(MediaType.APPLICATION_JSON).content(badJson))
        .andExpect(status().isBadRequest()); // Bean Validation → 400 확인
}
```

### 원칙 5 — 동시성 시연 테스트

`synchronized` 유무에 따른 결과 차이를 **100 스레드 × 1,000 증가**로 실증합니다.

```java
@Test
void unsafe_카운터_손실_발생() throws InterruptedException {
    // synchronized 없음 → race condition → 최종값 < 100,000
    assertThat(counter.get()).isLessThan(TOTAL);
}

@Test
void safe_카운터_정확한_값() throws InterruptedException {
    // synchronized 적용 → 정확히 100,000
    assertThat(counter.get()).isEqualTo(TOTAL);
}
```

---

## 테스트 커버리지

### 전체 현황

| 총 테스트 클래스 | 총 테스트 수 | 통과율 |
|---------------|------------|-------|
| 24개 | 118개 | 100% |

### 클래스별 테스트 수

| 테스트 클래스 | 종류 | 테스트 수 | 검증 내용 |
|-------------|------|---------|----------|
| `WrongNoteTest` | 엔티티 | 5 | SM-2 경계값, easeFactor 하한, 연속 복습 |
| `UserStatTest` | 엔티티 | 8 | 난이도 임계값, 5배수 조정, 이동 평균 |
| `DifficultyTest` | 열거형 | 7 | upgrade/downgrade 상·하한 고정 |
| `AnswerServiceTest` | 서비스 | 3 | 정답/오답 처리, 이벤트 발행 검증 |
| `WrongNoteServiceTest` | 서비스 | 7 | CRUD, 중복 방지, 예외 처리 |
| `UserStatServiceTest` | 서비스 | 4 | 신규 생성, 기존 갱신, 404 예외 |
| `CourseServiceTest` | 서비스 | 4 | 수강신청, 기존 코스 종료, Strategy 디스패치 |
| `CoursePolicyTest` | 서비스 | 7 | 3가지 Policy 추천 로직, 중복 제외 |
| `WrongNoteRegistrationListenerTest` | 이벤트 | 2 | 정답/오답 이벤트 → 올바른 서비스 호출 |
| `UserStatUpdateListenerTest` | 이벤트 | 2 | 이벤트 → UserStat.recordAnswer 호출 |
| `MailNotificationListenerTest` | 이벤트 | 2 | 오답 시만 메일 발송 |
| `MockMailSenderTest` | 인프라 | 3 | 발송 기록, 목록 조회 |
| `QuestionRecommenderFactoryTest` | 인프라 | 5 | Factory 생성, 범위 교차 없음 |
| `LegacyQuestionAdapterTest` | 어댑터 | 7 | Adapter 패턴 동작, 카테고리 필터 |
| `UserStatRaceConditionTest` | 동시성 | 2 | unsafe 손실 발생 vs safe 정확성 |
| `WrongNoteRepositoryTest` | Repository | 6 | UniqueConstraint, 페이징, due 쿼리 |
| `UserStatRepositoryTest` | Repository | 6 | @Version 초기값, 낙관적 락, 중복 방지 |
| `AnswerRepositoryTest` | Repository | 4 | Top20 쿼리, 사용자 격리 |
| `CourseEnrollmentRepositoryTest` | Repository | 5 | 활성 코스 조회, 종료 후 재수강 |
| `AnswerControllerTest` | Controller | 4 | 201 Created, Bean Validation 400 |
| `UserStatControllerTest` | Controller | 3 | 200 OK, 404 처리, 파라미터 누락 400 |
| `WrongNoteControllerTest` | Controller | 4 | 페이지 응답, due 목록, 404 처리 |
| `CourseControllerTest` | Controller | 4 | 수강신청, 오늘의 문제, 404 처리 |
| `LearningFlowE2ETest` | E2E | 6 | 오답→노트 생성, SM-2 복습, 코스 재수강, 중복 방지 |

---

## 주요 기능 설명

### 1. 답안 제출 & 이벤트 처리
```
POST /api/answers
{ "userEmail": "user@example.com", "questionId": 1, "isCorrect": true, "score": 85, "responseTimeMs": 12000 }
```
- 답안 저장 후 `AnswerSubmittedEvent` 발행
- 3개의 `@TransactionalEventListener(AFTER_COMMIT)` 리스너가 독립적으로 처리:
  - 오답 노트 등록/삭제
  - 학습 통계 갱신 + 난이도 자동 조정
  - 복습 알림 메일 비동기 발송

### 2. SM-2 간격 반복 복습
```
POST /api/wrong-notes/{id}/review  { "isCorrect": true }
GET  /api/wrong-notes/me/due?email=user@example.com   (오늘 복습할 문제 목록)
```
| 결과 | interval | easeFactor |
|------|----------|------------|
| 정답 | `round(interval × ease)` | `ease + 0.1` |
| 오답 | 1 (리셋) | `max(1.3, ease - 0.2)` |

### 3. 난이도 자동 조정
최근 20문제 정답률 기준으로 `UserStat.currentDifficulty` 자동 변경:
- `> 80%` → 한 단계 상승 (EASY → MEDIUM → HARD)
- `< 40%` → 한 단계 하강

### 4. 코스 수강 & 오늘의 문제
```
POST /api/courses/enroll  { "userEmail": "...", "courseType": "SHORT_INTENSIVE" }
GET  /api/courses/me/today?email=user@example.com
```
Strategy 패턴으로 코스 타입(`SHORT_INTENSIVE` / `HARD_ONLY` / `WEAKNESS_FOCUSED`)에 따라 다른 문제 선택 로직 적용

### 5. 학습 통계 조회
```
GET /api/stats/me?email=user@example.com
```

---

## 본인이 구현한 부분

기존 매일메일 코드(mail-core, mail-api 등)는 **수정 없이** 그대로 두고,  
`learning-api` 모듈 전체를 새로 작성했습니다.

- **Gradle 멀티모듈 통합**: `settings.gradle`에 `learning-api` 추가, `build-recipe-plugin` 타입 기반 의존 설정, Datadog 미터 제외 처리
- **도메인 설계**: Answer / WrongNote / UserStat / CourseEnrollment 4개 JPA 엔티티
- **SM-2 알고리즘**: `WrongNote.applyReview()` — 학습 과학 기반 복습 주기 계산
- **동시성 처리**: `synchronized` + `@Version` 이중 보호, Race Condition 시연 테스트
- **이벤트 아키텍처**: `ApplicationEventPublisher` + `@TransactionalEventListener(AFTER_COMMIT)` — 트랜잭션 안전성 보장
- **4가지 GoF 패턴** 구현: Adapter × 2, Strategy × 3, Observer, Factory
- **비동기 메일**: `ThreadPoolTaskExecutor` + `@Async` 스레드풀 분리
- **프로파일 전략**: local/test(Mock) ↔ dev(SMTP) 환경 분리
- **전체 테스트**: 도메인별 단위 테스트 + Race Condition 시연 + Factory/Adapter 통합 테스트

---

## AI 활용 여부 및 활용 범위

**바이브코딩(Vibe Coding) 방식으로 Claude Code를 활용**하여 개발했습니다.

| 항목 | 내용 |
|------|------|
| 활용 도구 | Claude Code (claude.ai/code) — CLI 기반 AI 에이전트 |
| 활용 방식 | 바이브코딩 — 설계 의도와 패턴 목표를 지시하면 AI가 코드 생성, 테스트, 커밋까지 자율 수행 |
| 설계 결정 | 모든 아키텍처 결정(패턴 선택, 모듈 분리 방식, 동시성 전략)은 직접 기획 |
| AI 역할 | 기획된 설계를 Java 코드로 구현, 오류 디버깅, 테스트 작성, PR 생성 자동화 |
| 직접 작성 | 비즈니스 요구사항, 설계 문서(DECISIONS.md, architecture.md), 패턴 선택 근거 |

> SM-2 알고리즘, 동시성 전략, 이벤트 기반 아키텍처 등 핵심 설계는 직접 기획하였으며,  
> AI는 해당 설계를 코드로 구현하는 역할을 담당했습니다.

---

## 실행 화면

> 아래 캡처를 추가해 주세요.

**캡처 1**: 로컬 서버 실행 화면 (`./gradlew :learning-api:bootRun --args='--spring.profiles.active=local'`)

```
[ 캡처 이미지 삽입 ]
```

**캡처 2**: 테스트 전체 통과 화면 (`./gradlew :learning-api:test`)

```
[ 캡처 이미지 삽입 ]
```
