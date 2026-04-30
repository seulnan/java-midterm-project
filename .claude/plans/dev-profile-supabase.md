# dev 프로파일 Supabase 연결 (dev-profile-supabase)

## 목표
application-dev.yml 추가, .env 연동, dev 프로파일 기동 시 Supabase PostgreSQL 연결 확인

## 영향 범위
- 새 파일:
  - learning-api/src/main/resources/application-dev.yml
  - .env.example (Supabase/SMTP 키 이름만, 값 없음)
- 수정 파일: 없음
- 영향 X 모듈: 기존 모듈 전부

## 단계 (체크리스트)

- [ ] 1. application-dev.yml 작성
  - datasource: ${SUPABASE_DB_URL}, ${SUPABASE_DB_USERNAME}, ${SUPABASE_DB_PASSWORD}
  - hibernate ddl-auto: update
  - PostgreSQLDialect
  - mail sender: smtp
- [ ] 2. .env.example 작성 (값 없이 키 이름만)
- [ ] 3. application-dev.yml 에 SMTP 설정 추가

## 테스트 범위
- [ ] 컴파일 검증: ./gradlew :learning-api:compileJava

## 디자인 패턴
해당 없음 (설정 파일)

## 주의사항
- .env 파일 자체는 수정하지 않음 (읽기만)
- .env.example 은 커밋 가능 (실제 값 없음)
- Supabase PostgreSQL URL 형식: jdbc:postgresql://... (JDBC 형식으로 변환 필요)
  .env의 SUPABASE_DB_URL은 postgresql:// 형식이므로 yml에서 jdbc: 접두사 추가
