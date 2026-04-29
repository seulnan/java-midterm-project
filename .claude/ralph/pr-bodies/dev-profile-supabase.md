## 무엇을
learning-api dev 프로파일 설정 추가 — Supabase PostgreSQL 연결 및 Gmail SMTP 설정

## 왜
local 프로파일은 H2 인메모리로 기능 개발에 적합하지만, 실제 시연을 위해서는 영속적인 DB와 실제 메일 발송이 필요하다. 사용자가 사전에 채운 .env 파일의 Supabase/Gmail 자격증명을 spring profile로 연결해 `--spring.profiles.active=dev` 한 줄로 전환 가능하도록 한다.

## 어떻게
- `application-dev.yml`: Supabase PostgreSQL pooler URL 직접 참조 (호스트 하드코딩, 비밀번호만 `${SUPABASE_DB_PASSWORD}` 환경변수 참조)
- `hibernate.ddl-auto: update` — dev 재시작 시 스키마 자동 갱신
- `.env.example`: 실제 값 없이 키 이름만 제공 (협업/온보딩 문서화 목적)
- `learning.mail.sender: smtp` 플래그 — MailConfig가 이 값을 보고 SmtpMailSender 빈 활성화 (mail-strategy slug에서 구현)

## 고려했으나 채택하지 않은 대안

- **대안**: `${SUPABASE_DB_URL}`을 그대로 Spring datasource url로 사용
  - **장점**: .env 한 곳에서만 관리
  - **기각 이유**: .env의 URL 형식이 `postgresql://user:pass@host/db`로 JDBC 형식(`jdbc:postgresql://`)이 아님. Spring Boot가 자동 변환을 지원하지 않아 런타임 오류 발생. 직접 JDBC URL을 명시하는 것이 더 명확하다.

- **대안**: `spring.datasource.url`을 `application.yml`(공통)에 넣고 환경변수로 덮어쓰기
  - **장점**: 설정 파일 한 곳 관리
  - **기각 이유**: local/test/dev 각 프로파일이 완전히 다른 DB(H2/Supabase)를 사용하므로 프로파일 분리가 옳다.

## 의도적으로 구현하지 않은 것

- Supabase 연결 통합 테스트 자동화
  - **이유**: dev DB 자격증명이 .env에만 있어 CI 환경에서 실행 불가. GitHub Actions에 시크릿 등록 필요.
  - **운영 시 검토 대상**: GitHub Secrets에 SUPABASE_DB_PASSWORD 등록 후 `@ActiveProfiles("dev")` 통합 테스트 추가.

## 트레이드오프 요약
| 결정 | 얻은 것 | 잃은 것 |
|---|---|---|
| 호스트 하드코딩 + 비밀번호만 환경변수 | 구동 즉시 연결 가능, 설정 단순 | Supabase 프로젝트 변경 시 yml 수정 필요 |
| ddl-auto: update | 스키마 자동 반영 | 컬럼 삭제/타입 변경 시 자동 처리 안 됨 |

## 변경 사항
- 추가: `learning-api/src/main/resources/application-dev.yml`
- 추가: `.env.example`
