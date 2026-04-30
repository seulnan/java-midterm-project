## 무엇을
UserStat 도메인 구현 — 사용자별 학습 통계, 난이도 자동 조정, Race Condition 시연 테스트

## 왜
답안 제출마다 통계를 누적해야 난이도 적응이 가능하다. 동시에 여러 요청이 같은 사용자 통계를 수정할 때 갱신 손실이 발생하면 정확한 통계가 불가능하다. `synchronized` 메서드와 JPA `@Version`(낙관적 락)을 함께 적용해 동시성 안전성을 확보한다.

## 어떻게
- `UserStat.recordAnswer()`: `synchronized` 인스턴스 메서드 — 단일 JVM 내 동시 갱신 직렬화
- `@Version Long version`: 낙관적 락 — 동시 트랜잭션이 같은 버전을 변경하면 `OptimisticLockException` 발생
- `UserStatService.recordAnswer()`: 서비스 레벨에도 `synchronized` 추가 (findOrCreate + update 복합 연산 원자화)
- `Difficulty.upgrade()/downgrade()`: 정답률 0.8/0.4 임계값 기반 자동 조정
- `UserStatRaceConditionTest`: 100 스레드 × 1000 증가 = 100,000 기대값으로 unsafe/safe 비교 시연

## 고려했으나 채택하지 않은 대안

- **대안**: `@Lock(LockModeType.PESSIMISTIC_WRITE)` 비관적 락
  - **장점**: DB 레벨 잠금으로 확실한 동시성 보장
  - **기각 이유**: 학습 API는 읽기 비율이 높아 비관적 락은 성능 저하 과도. `synchronized` + `@Version` 조합이 충분하며 교육적으로도 두 패턴을 병렬 시연할 수 있다.

- **대안**: 통계 갱신을 별도 배치로 비동기 처리
  - **장점**: 답안 제출 응답 시간 단축
  - **기각 이유**: 통계가 즉시 반영되어야 난이도 조정이 실시간으로 작동한다. 비동기 처리 시 다음 문제 추천이 직전 답안을 반영하지 못할 수 있다.

## 의도적으로 구현하지 않은 것

- 최근 20문제 정답률 계산 (별도 Repository 쿼리)
  - **이유**: 현재는 전체 정답률로 근사. 정확한 최근 20개는 `AnswerRepository`에서 조회해야 하며 서비스 간 의존이 생김. 기능 시연에는 근사값으로 충분.
  - **운영 시 검토 대상**: Sliding window 집계 쿼리 추가 또는 답안 제출 시 이벤트로 최근 N개 유지.

## 트레이드오프 요약
| 결정 | 얻은 것 | 잃은 것 |
|---|---|---|
| synchronized + @Version 이중 적용 | 단일 JVM + 다중 노드 모두 대응 가능 | 단일 노드에서는 synchronized만으로 충분, 중복 |
| 서비스 레벨 synchronized | findOrCreate 복합 연산 원자화 | 해당 서비스 메서드 병렬 처리 불가 |

## Race Condition 시연 결과
```
[UNSAFE] expected=100000  actual=14259  ← 85,741건 손실
[SAFE]   expected=100000  actual=100000 ← 손실 없음
```

## 변경 사항
- 추가: `domain/userstat/` (Difficulty, UserStat, UserStatRepository, UserStatService, DTO)
- 추가: `api/UserStatController`
- 추가: `UserStatRaceConditionTest` (unsafe/safe 시연, README 캡처용)
