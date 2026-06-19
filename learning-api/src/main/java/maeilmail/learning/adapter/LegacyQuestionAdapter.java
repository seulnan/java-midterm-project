package maeilmail.learning.adapter;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.LongStream;
import org.springframework.stereotype.Component;

/**
 * mail-core의 Question 엔티티를 learning-api 도메인으로 변환하는 어댑터.
 *
 * <p>Adapter 패턴: LegacyQuestionPort(Target)와 mail-core Question(Adaptee) 사이의 번역 계층.
 * learning-api는 LegacyQuestionPort에만 의존하고, 실제 데이터 소스는 이 어댑터가 캡슐화한다.
 *
 * <p>현재 구현은 인메모리 스텁이지만, 메일/데모에서 실제로 의미 있는 문제 텍스트가 노출되도록
 * 짝수 ID에는 백엔드, 홀수 ID에는 프론트엔드 면접 질문을 큐레이션해 담는다. 큐레이션 범위를
 * 벗어나는 ID는 동일 패턴의 일반 문제로 채워 총 200문제를 유지한다.
 * 공유 스프링 컨텍스트 배포 시 maeilmail.question.QuestionRepository를 직접 주입하여
 * 실제 DB 데이터를 반환하도록 교체 가능하다.
 */
@Component
public class LegacyQuestionAdapter implements LegacyQuestionPort {

    private static final int TOTAL = 200;

    // 짝수 ID = BACKEND, 홀수 ID = FRONTEND (카테고리 패리티 유지)
    private static final Map<Long, String[]> CURATED = Map.ofEntries(
            // --- BACKEND (짝수 ID) ---
            Map.entry(2L, new String[]{"트랜잭션의 ACID 속성을 설명해주세요.",
                    "원자성(Atomicity), 일관성(Consistency), 격리성(Isolation), 지속성(Durability)이 각각 무엇을 보장하는지와, 격리성이 깨질 때 발생하는 현상(Dirty/Non-repeatable/Phantom Read)을 함께 설명해보세요."}),
            Map.entry(4L, new String[]{"데이터베이스 인덱스는 왜 조회를 빠르게 하나요? 단점은?",
                    "B-Tree 인덱스가 정렬된 구조로 탐색 범위를 좁히는 원리와, 쓰기 비용 증가·디스크 사용량·카디널리티가 낮은 컬럼에서의 비효율 같은 트레이드오프를 설명해보세요."}),
            Map.entry(6L, new String[]{"HTTP와 HTTPS의 차이는 무엇인가요?",
                    "TLS 핸드셰이크를 통한 암호화·무결성·인증의 보장과, 대칭키/비대칭키가 핸드셰이크에서 어떻게 함께 쓰이는지 설명해보세요."}),
            Map.entry(8L, new String[]{"REST API에서 PUT과 PATCH의 차이는?",
                    "전체 교체(PUT)와 부분 수정(PATCH)의 의미 차이, 그리고 멱등성(idempotency) 관점에서 두 메서드가 어떻게 다른지 설명해보세요."}),
            Map.entry(10L, new String[]{"JVM 가비지 컬렉션의 동작 방식을 설명해주세요.",
                    "객체 도달 가능성(reachability) 기반 수거, Young/Old 영역 분리와 Minor/Major GC, Stop-the-World가 무엇인지 설명해보세요."}),
            Map.entry(12L, new String[]{"프로세스와 스레드의 차이는 무엇인가요?",
                    "메모리 공간(코드/데이터/힙/스택) 공유 여부, 컨텍스트 스위칭 비용, 그리고 멀티스레드에서 공유 자원 접근 시 동기화가 필요한 이유를 설명해보세요."}),
            Map.entry(14L, new String[]{"낙관적 락과 비관적 락의 차이와 사용 시점은?",
                    "충돌이 드물 때 버전 컬럼으로 검증하는 낙관적 락과, 충돌이 잦을 때 행을 선점하는 비관적 락의 트레이드오프, 그리고 각 전략이 적합한 상황을 설명해보세요."}),
            Map.entry(16L, new String[]{"JPA N+1 문제가 무엇이고 어떻게 해결하나요?",
                    "지연 로딩 연관관계를 순회할 때 쿼리가 N+1번 나가는 원인과, fetch join·@EntityGraph·배치 사이즈 같은 해결책의 차이를 설명해보세요."}),
            Map.entry(18L, new String[]{"TCP 3-way handshake 과정을 설명해주세요.",
                    "SYN → SYN/ACK → ACK로 연결을 수립하는 과정과, 각 단계에서 시퀀스 번호가 왜 필요한지 설명해보세요."}),
            Map.entry(20L, new String[]{"캐시 무효화(cache invalidation) 전략에는 무엇이 있나요?",
                    "TTL 만료, write-through/write-back, 그리고 데이터 변경 시 캐시를 갱신/삭제하는 방식의 장단점과 정합성 문제를 설명해보세요."}),
            // --- FRONTEND (홀수 ID) ---
            Map.entry(1L, new String[]{"브라우저 렌더링 과정(Critical Rendering Path)을 설명해주세요.",
                    "DOM·CSSOM 생성 → Render Tree → Layout → Paint → Composite로 이어지는 단계와, 각 단계가 성능에 미치는 영향을 설명해보세요."}),
            Map.entry(3L, new String[]{"이벤트 버블링과 캡처링의 차이는?",
                    "이벤트가 전파되는 두 방향과 addEventListener의 capture 옵션, 그리고 이벤트 위임(delegation)이 왜 유용한지 설명해보세요."}),
            Map.entry(5L, new String[]{"클로저(closure)란 무엇이고 언제 쓰나요?",
                    "함수가 선언될 때의 렉시컬 환경을 기억하는 특성과, 데이터 은닉·상태 유지·콜백에서의 활용 예를 설명해보세요."}),
            Map.entry(7L, new String[]{"Virtual DOM이 무엇이고 왜 빠른가요?",
                    "실제 DOM 조작 비용을 줄이기 위한 메모리상 트리와 diffing/reconciliation 과정, 그리고 '항상 빠른 것은 아니다'라는 점을 설명해보세요."}),
            Map.entry(9L, new String[]{"CORS는 무엇이고 왜 발생하나요?",
                    "동일 출처 정책(SOP)과, 브라우저가 교차 출처 요청 시 보내는 preflight(OPTIONS) 및 서버의 허용 헤더(Access-Control-Allow-*)를 설명해보세요."}),
            Map.entry(11L, new String[]{"쿠키, 세션, 로컬스토리지의 차이는?",
                    "저장 위치(클라이언트/서버), 만료·용량, 서버 전송 여부, 그리고 인증 토큰을 보관할 때의 보안 고려사항을 설명해보세요."}),
            Map.entry(13L, new String[]{"debounce와 throttle의 차이는?",
                    "마지막 호출만 실행(debounce)과 일정 간격으로 제한(throttle)의 차이, 그리고 검색 입력·스크롤 같은 실제 사용 예를 설명해보세요."}),
            Map.entry(15L, new String[]{"Promise와 async/await의 관계를 설명해주세요.",
                    "async/await가 Promise 기반 비동기를 동기식 문법으로 표현하는 문법적 설탕이라는 점과, 에러 처리(try/catch)·병렬 실행(Promise.all)을 설명해보세요."}),
            Map.entry(17L, new String[]{"리플로우(reflow)와 리페인트(repaint)의 차이는?",
                    "레이아웃을 다시 계산하는 reflow와 시각적 속성만 다시 그리는 repaint의 비용 차이, 그리고 reflow를 줄이는 방법을 설명해보세요."}),
            Map.entry(19L, new String[]{"== 와 === 의 차이는?",
                    "타입 강제 변환(coercion)을 수행하는 느슨한 동등(==)과 타입까지 비교하는 엄격한 동등(===)의 차이, 그리고 === 사용을 권장하는 이유를 설명해보세요."})
    );

    private final Map<Long, LegacyQuestion> store;

    public LegacyQuestionAdapter() {
        this.store = buildInMemoryStore();
    }

    @Override
    public Optional<LegacyQuestion> findById(Long id) {
        return Optional.ofNullable(store.get(id));
    }

    @Override
    public List<LegacyQuestion> findByIds(List<Long> ids) {
        return ids.stream()
                .map(store::get)
                .filter(q -> q != null)
                .toList();
    }

    @Override
    public List<LegacyQuestion> findByCategory(String category) {
        return store.values().stream()
                .filter(q -> q.category().equalsIgnoreCase(category))
                .toList();
    }

    private Map<Long, LegacyQuestion> buildInMemoryStore() {
        return LongStream.rangeClosed(1, TOTAL)
                .mapToObj(this::toQuestion)
                .collect(Collectors.toMap(LegacyQuestion::id, q -> q));
    }

    private LegacyQuestion toQuestion(long id) {
        String category = id % 2 == 0 ? "BACKEND" : "FRONTEND";
        String[] curated = CURATED.get(id);
        if (curated != null) {
            return new LegacyQuestion(id, curated[0], curated[1], category);
        }
        String topic = category.equals("BACKEND") ? "백엔드" : "프론트엔드";
        return new LegacyQuestion(
                id,
                topic + " 면접 예상 질문 #" + id,
                "이 문제에 대한 핵심 개념을 본인의 언어로 설명해보세요. (문제 ID " + id + ")",
                category
        );
    }
}
