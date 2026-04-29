---
name: code
description: "plan slug 받아 구현→리뷰→테스트→커밋→PR 파이프라인 실행."
---

# code

입력: plan slug

1. git checkout main && git pull origin main
2. git checkout -b feature/{slug}
3. implementer 호출 (계획 기반 구현 + compileJava)
   - 컴파일 3회 실패 → 사용자 보고 후 중단
4. reviewer 호출 (4축 리뷰)
   - FAIL → implementer 로 수정 (최대 3회)
5. tester 호출 (테스트 작성 + 실행)
6. git commit
7. git push origin feature/{slug}
8. gh pr create --repo seulnan/java-midterm-project --base main --head feature/{slug} \
     --title "{prefix}: {요약}" \
     --body-file .claude/ralph/pr-bodies/{slug}.md
