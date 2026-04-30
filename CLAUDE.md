# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build & Test Commands

```bash
# Compile only learning-api (fast feedback)
./gradlew :learning-api:compileJava

# Run all learning-api tests
./gradlew :learning-api:test

# Run a single test class
./gradlew :learning-api:test --tests "maeilmail.learning.domain.answer.*"
./gradlew :learning-api:test --tests "maeilmail.learning.domain.wrongnote.*"
./gradlew :learning-api:test --tests "maeilmail.learning.domain.userstat.*"
./gradlew :learning-api:test --tests "maeilmail.learning.event.*"
./gradlew :learning-api:test --tests "maeilmail.learning.infrastructure.recommender.*"
./gradlew :learning-api:test --tests "maeilmail.learning.adapter.*"

# Start learning-api locally (H2, MockMailSender)
./gradlew :learning-api:bootRun --args='--spring.profiles.active=local'

# Full build (skip tests)
./gradlew build -x test

# Build learning-api only
./gradlew :learning-api:build
```

A **PostToolUse hook** (`.claude/hooks/post-tool-java.sh`) auto-runs `:learning-api:compileJava` after every Java file edit in `learning-api/` — compilation errors surface immediately without running tests.

## Repository Structure

This is a **Gradle multi-module monorepo** using `com.linecorp.build-recipe-plugin`. Each submodule declares its type in `gradle.properties` (`type=java-boot-mvc-application`, `type=java-boot-data-mail-lib`, etc.), and the root `build.gradle` applies dependency recipes based on those types via `configureByTypeHaving(project, [...])`.

### Modules

| Module | Type | Purpose |
|--------|------|---------|
| `mail-core` | `java-boot-data-mail-lib` | Shared JPA entities: `Question`, `Subscribe`, base `MailSender` |
| `mail-api` | application | Subscription REST API (port 8080) |
| `mail-app` | application | Main runnable app |
| `mail-batch` | batch | Daily mail send job |
| `mail-admin` | application | Admin functions |
| `wiki-core` / `wiki-api` | lib / application | Wiki feature |
| **`learning-api`** | `java-boot-mvc-application` | **Adaptive learning backend (port 8081) — this project's new module** |

**The original modules (`mail-*`, `wiki-*`) must never be modified.**

## learning-api Architecture

Base package: `maeilmail.learning`. Standalone Spring Boot app, independent of `mail-app`.

### Layer layout

```
maeilmail.learning
├── LearningApiApplication        @SpringBootApplication, @EnableJpaAuditing, @EnableAsync, @EnableScheduling
├── config/                       AsyncConfig (mailExecutor pool), MailConfig (profile-based bean), SchedulerConfig
├── common/                       ApiResponse<T> wrapper, GlobalExceptionHandler, ErrorCode
├── domain/
│   ├── answer/                   Answer entity + AnswerService + AnswerSubmittedEvent
│   ├── wrongnote/                WrongNote (SM-2) + WrongNoteService
│   ├── userstat/                 UserStat (@Version optimistic lock) + synchronized service
│   └── course/                   CourseEnrollment + CourseService + policy/ (Strategy)
├── infrastructure/
│   ├── mail/                     LearningMailSender interface + SmtpMailSender + MockMailSender
│   └── recommender/              QuestionRecommender interface + Easy/Medium/Hard + Factory
├── adapter/                      LegacyQuestionPort interface + LegacyQuestionAdapter (Adapter pattern)
├── api/                          REST controllers (Answer, WrongNote, UserStat, Course, Dev)
└── event/listener/               WrongNoteRegistrationListener, UserStatUpdateListener, MailNotificationListener
```

### Design patterns

| Pattern | Location | Key mechanic |
|---------|----------|--------------|
| **Adapter** | `infrastructure/mail/LearningMailSender` | `@Profile`-selected bean: `MockMailSender` (local/test) vs `SmtpMailSender` (dev) |
| **Adapter** | `adapter/LegacyQuestionPort` + `LegacyQuestionAdapter` | Isolates `learning-api` from `mail-core` entity model |
| **Strategy** | `domain/course/policy/CoursePolicy` | Spring injects `List<CoursePolicy>`; `CourseService` dispatches via `Map<CourseType, CoursePolicy>` |
| **Observer** | `AnswerSubmittedEvent` + 3 listeners | `@TransactionalEventListener(phase = AFTER_COMMIT)` — fires only after DB commit |
| **Factory** | `QuestionRecommenderFactory` | Spring injects `List<QuestionRecommender>`; factory builds `Map<Difficulty, QuestionRecommender>` |

### Concurrency model (`UserStat`)
- `synchronized` on `recordAnswer()` + service-level `findOrCreate` — single-JVM guard
- `@Version Long version` — DB-level optimistic lock; throws `OptimisticLockException` on collision
- `UserStatRaceConditionTest`: 100 threads × 1000 increments = 100,000 exact — demonstrates safe vs unsafe counter

### Event flow (answer submission)
```
POST /api/answers
  └── AnswerService.submitAnswer()
        ├── save(Answer)
        └── publish(AnswerSubmittedEvent)
              ├── [AFTER_COMMIT] WrongNoteRegistrationListener  — upsert/remove WrongNote
              ├── [AFTER_COMMIT] UserStatUpdateListener          — update difficulty
              └── [AFTER_COMMIT + @Async("mailExecutor")] MailNotificationListener
```

### SM-2 spaced repetition (WrongNote)
- Correct: `interval = round(interval × ease)`, `ease += 0.1`
- Wrong: `interval = 1` (reset), `ease = max(1.3, ease - 0.2)`
- Initial: `interval=1`, `ease=2.5`

### Difficulty auto-adjustment (UserStat)
Based on last 20 questions: `> 80%` correct → upgrade, `< 40%` → downgrade.

## git / GitHub workflow

- **Remote**: `origin` = `seulnan/java-midterm-project` (fork), `upstream` = `maeil-mail/maeil-mail-be`
- `gh` CLI defaults to `upstream` context — **always** pass `--repo seulnan/java-midterm-project` to `gh pr create`
- Feature branches stack on each other (each based on the previous feature branch) because PRs are not merged to main
- Never commit directly to `main`; never force-push; never modify `.env`

## Profiles & environment

| Profile | DB | Mail | Notes |
|---------|-----|------|-------|
| `local` | H2 in-memory | `MockMailSender` (console log) | Default for dev |
| `test` | H2 in-memory | `MockMailSender` (in-memory log) | Used by `@SpringBootTest` |
| `dev` | Supabase PostgreSQL | `SmtpMailSender` (Gmail SMTP) | Requires `.env` |

`.env` variables (gitignored — see `env.example`): `SUPABASE_URL`, `SUPABASE_USERNAME`, `SUPABASE_PASSWORD`, `MAIL_USERNAME`, `MAIL_PASSWORD`.

## learning-api build quirk

`type=java-boot-mvc-application` causes the root recipe to inject `micrometer-registry-datadog`. `learning-api/build.gradle` excludes it explicitly:

```groovy
configurations.all {
    exclude group: 'io.micrometer', module: 'micrometer-registry-datadog'
}
```

Do not remove this exclusion.
