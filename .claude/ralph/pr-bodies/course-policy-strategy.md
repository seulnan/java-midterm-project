## 무엇을
Strategy 패턴으로 코스 정책 3종 구현 — SHORT_INTENSIVE / HARD_ONLY / WEAKNESS_FOCUSED

## 왜
사용자마다 학습 목표가 다르다. 집중 단기 학습, 어려운 문제만 풀기, 약점 보완 중 어떤 전략을 선택하느냐에 따라 오늘의 추천 문제가 달라져야 한다. 전략마다 `if-else`를 쌓으면 새 정책 추가 시 `CourseService`를 수정해야 한다. `CoursePolicy` 인터페이스와 Spring의 `List<CoursePolicy>` 자동 수집으로 OCP를 지키면서 정책 추가가 가능하다.

## 어떻게
- `CoursePolicy` 인터페이스: `courseType()` + `recommendQuestionIds()`
- 3개 구현체는 `@Component`로 등록 → Spring이 `List<CoursePolicy>` 자동 주입
- `CourseService.policyMap()`: `CourseType → CoursePolicy` 맵으로 O(1) 디스패치
- `CourseEnrollment.end()`: 신규 등록 시 기존 활성 코스 자동 종료

## 고려했으나 채택하지 않은 대안

- **대안**: `switch(courseType)` 문으로 정책 분기
  - **장점**: 코드 한 곳에서 파악 가능
  - **기각 이유**: 정책 추가 시 `CourseService`를 수정해야 하며 OCP 위반. Strategy 패턴은 정책 추가를 새 클래스 추가만으로 가능하게 한다.

- **대안**: 각 정책을 DB 테이블로 관리 (플러그인 방식)
  - **장점**: 코드 배포 없이 정책 변경 가능
  - **기각 이유**: 정책 로직이 Java 코드에 있어야 타입 안전성과 테스트가 가능. 운영 단계에서 필요하면 도입.

## 의도적으로 구현하지 않은 것

- 실제 Question DB에서 문제 ID 조회 (LegacyQuestionPort 연결)
  - **이유**: adapter-original-domain slug에서 원본 Question 도메인을 연결. 현재는 가상 ID 범위로 동작.
  - **운영 시 검토 대상**: LegacyQuestionPort로 카테고리/난이도별 문제 풀 조회.

## 트레이드오프 요약
| 결정 | 얻은 것 | 잃은 것 |
|---|---|---|
| List<CoursePolicy> 자동 수집 | 정책 추가 시 Service 무변경 | 정책 등록 여부를 런타임에 확인해야 함 |
| 기존 코스 자동 종료 | 활성 코스 1개 보장 | 코스 이력 추적 불가 (endedAt으로 조회는 가능) |

## 변경 사항
- 추가: `domain/course/` (CourseType, CourseEnrollment, CourseEnrollmentRepository, CourseService, DTO)
- 추가: `domain/course/policy/` (CoursePolicy 인터페이스 + 3개 구현체)
- 추가: `api/CourseController` (/api/courses/enroll, /api/courses/me/today)
- 추가: `CoursePolicyTest` (4 테스트 케이스)
