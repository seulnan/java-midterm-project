package maeilmail.learning.domain.grading;

import java.util.List;
import java.util.Map;

/**
 * 질문별 채점 기준(모범답안 + 핵심 개념).
 *
 * <p>현재는 데모/백엔드 트랙에서 쓰는 질문들에 대해 직접 큐레이션한 정적 데이터다.
 * 실제 서비스에선 문제 등록 시 함께 입력받아 DB에 저장하는 형태로 확장할 수 있다.
 */
final class GradingKeys {

    private GradingKeys() {
    }

    private static Concept c(String label, String why, String... keywords) {
        return new Concept(label, List.of(keywords), why);
    }

    private static final Map<Long, QuestionKey> KEYS = Map.ofEntries(
            Map.entry(2L, new QuestionKey(
                    "ACID는 원자성·일관성·격리성·지속성. 원자성=전부 성공 또는 전부 실패, 격리성=동시 트랜잭션이 서로 간섭하지 않음, 지속성=커밋되면 영구 보존.",
                    List.of(
                            c("원자성(전부 아니면 전무)", "하나라도 실패하면 전체를 롤백해 '일부만 반영'을 막습니다.",
                                    "원자", "atomic", "롤백", "전부", "전무", "all or nothing"),
                            c("격리성(동시 트랜잭션 간섭 차단)", "동시 트랜잭션이 서로의 중간 상태를 못 보게 합니다 — 깨지면 Dirty/Phantom Read.",
                                    "격리", "isolation", "동시", "고립"),
                            c("지속성(커밋 후 영구 보존)", "커밋된 결과는 장애가 나도 유지돼야 합니다(로그·디스크 반영).",
                                    "지속", "durab", "영구", "영속")))),
            Map.entry(4L, new QuestionKey(
                    "인덱스는 정렬된 B-Tree로 탐색 범위를 좁혀 조회를 빠르게 한다. 대신 쓰기마다 인덱스도 갱신해 쓰기 비용이 늘고, 카디널리티가 낮은 컬럼엔 비효율적.",
                    List.of(
                            c("정렬된 구조로 탐색범위 축소(B-Tree)", "정렬된 트리라 전체 스캔 대신 범위를 좁혀 찾습니다.",
                                    "b-tree", "btree", "정렬", "트리", "탐색", "범위"),
                            c("쓰기 비용 증가 트레이드오프", "조회는 빨라지지만 INSERT/UPDATE마다 인덱스도 갱신해 쓰기가 느려집니다.",
                                    "쓰기", "삽입", "갱신", "insert", "update", "비용"),
                            c("낮은 카디널리티에서 비효율", "값 종류가 적은 컬럼(예: 성별)은 인덱스 효과가 작습니다.",
                                    "카디널", "선택도", "중복", "cardinal")))),
            Map.entry(6L, new QuestionKey(
                    "HTTPS는 TLS로 암호화·무결성·서버 인증을 더한 HTTP. 핸드셰이크에서 비대칭키로 대칭키를 교환한 뒤 대칭키로 통신한다.",
                    List.of(
                            c("TLS 암호화", "평문 HTTP와 달리 도청을 막기 위해 전송을 암호화합니다.",
                                    "tls", "ssl", "암호화", "encrypt"),
                            c("무결성·서버 인증(인증서)", "인증서로 서버 신원을 검증하고 데이터 변조를 탐지합니다.",
                                    "인증서", "무결성", "인증", "certificate", "변조"),
                            c("대칭·비대칭키 혼용", "핸드셰이크는 비대칭키로 대칭키를 안전히 교환하고, 이후엔 빠른 대칭키로 통신합니다.",
                                    "대칭", "비대칭", "공개키", "개인키", "핸드셰이크", "handshake")))),
            Map.entry(8L, new QuestionKey(
                    "PUT은 리소스 전체 교체(멱등), PATCH는 부분 수정. PUT은 반복해도 결과가 같지만 PATCH는 구현에 따라 다를 수 있다.",
                    List.of(
                            c("전체 교체(PUT) vs 부분 수정(PATCH)", "PUT은 리소스를 통째로 바꾸고, PATCH는 일부 필드만 바꿉니다.",
                                    "전체", "부분", "교체", "replace"),
                            c("멱등성", "PUT은 여러 번 보내도 결과가 같습니다(멱등). PATCH는 보장되지 않을 수 있어요.",
                                    "멱등", "idempot", "반복"),
                            c("사용 시점", "일부 필드만 바꿀 땐 PATCH, 리소스 전체를 대체할 땐 PUT을 씁니다.",
                                    "필드", "일부", "부분 갱신")))),
            Map.entry(10L, new QuestionKey(
                    "GC는 도달 가능성 기준으로 더 이상 참조되지 않는 객체를 수거. Young/Old 세대로 나눠 Minor/Major GC를 돌리고, 수거 동안 Stop-the-world가 발생한다.",
                    List.of(
                            c("도달 가능성 기반 수거", "루트에서 참조로 도달 못 하는 객체를 쓰레기로 보고 회수합니다.",
                                    "도달", "reachab", "참조", "root"),
                            c("세대 분리(Young/Old)", "수명이 짧은 객체는 Young에서 자주(Minor), 오래 살면 Old로 옮겨 Major 대상이 됩니다.",
                                    "young", "old", "세대", "minor", "major", "eden"),
                            c("Stop-the-world", "수거 동안 애플리케이션 스레드가 잠시 멈춰 지연이 생길 수 있습니다.",
                                    "stop", "stw", "멈춤", "일시정지")))),
            Map.entry(12L, new QuestionKey(
                    "같은 프로세스의 스레드는 힙을 공유하고 스택은 따로 가진다. 스레드 전환이 더 가볍고, 메모리를 공유하므로 공유 자원 접근 시 동기화가 필요하다.",
                    List.of(
                            c("메모리 공유 여부(힙 공유·스택 별도)", "같은 프로세스의 스레드는 힙을 공유하고 스택은 따로 — 프로세스는 메모리가 독립입니다.",
                                    "메모리", "힙", "공유", "스택"),
                            c("컨텍스트 스위칭 비용", "스레드 전환이 프로세스 전환보다 가볍습니다(주소공간 전환이 없음).",
                                    "컨텍스트", "스위칭", "전환", "context"),
                            c("동기화 필요성", "메모리를 공유하므로 공유 자원 접근 시 동기화가 없으면 경쟁 상태가 생깁니다.",
                                    "동기화", "race", "임계", "lock", "경쟁", "뮤텍스")))),
            Map.entry(14L, new QuestionKey(
                    "낙관적 락은 충돌이 드물다고 보고 버전으로 검증, 비관적 락은 충돌이 잦다고 보고 행을 선점 잠금한다. 충돌 빈도에 따라 선택한다.",
                    List.of(
                            c("낙관적 락(버전 검증)", "충돌이 드물다고 보고, 커밋 시 버전이 바뀌었는지 검사해 충돌을 잡습니다.",
                                    "낙관", "optimist", "버전", "version"),
                            c("비관적 락(선점 잠금)", "충돌이 잦다고 보고 읽을 때부터 행을 잠가 다른 트랜잭션을 막습니다.",
                                    "비관", "pessimist", "잠금", "for update"),
                            c("사용 시점(충돌 빈도)", "충돌이 드물면 낙관락(오버헤드 적음), 잦으면 비관락이 유리합니다.",
                                    "충돌", "빈도", "드물", "잦")))),
            Map.entry(16L, new QuestionKey(
                    "지연 로딩 연관을 순회하면 쿼리가 N+1번 나간다. fetch join이나 @EntityGraph·배치 사이즈로 한 번에 가져와 해결한다.",
                    List.of(
                            c("원인: 지연 로딩 순회", "연관 엔티티를 지연 로딩으로 하나씩 접근하면 쿼리가 N+1번 나갑니다.",
                                    "지연", "lazy", "연관", "순회", "n+1"),
                            c("해결: fetch join", "연관을 한 번의 조인으로 함께 가져와 추가 쿼리를 없앱니다.",
                                    "fetch join", "페치", "조인", "join fetch"),
                            c("해결: batch size·@EntityGraph", "in 절로 묶어 가져오거나 @EntityGraph로 페치 계획을 지정합니다.",
                                    "batch", "배치", "entitygraph", "엔티티그래프"))))
    );

    static QuestionKey get(Long questionId) {
        return KEYS.get(questionId);
    }
}
