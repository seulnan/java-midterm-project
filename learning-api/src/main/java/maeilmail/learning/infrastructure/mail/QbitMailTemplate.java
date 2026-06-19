package maeilmail.learning.infrastructure.mail;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import maeilmail.learning.adapter.LegacyQuestion;

/**
 * QBit 발송 메일의 제목/본문(HTML)을 생성하는 템플릿.
 *
 * <p>이메일 클라이언트는 외부 CSS/style 태그를 대부분 무시하므로 인라인 스타일만 사용한다.
 * 발송 채널(Mock/SMTP)과 분리해 메일 콘텐츠만 단위로 검증·재사용할 수 있게 했다.
 */
public final class QbitMailTemplate {

    private static final String BRAND = "QBit";
    // 호스팅된 프런트엔드가 없는 백엔드 프로젝트이므로, CTA는 로컬에서 실제로 동작하는
    // 복습 조회 API(GET /api/wrong-notes/me/due)를 수신자 이메일로 개인화해 가리킨다.
    // (외부 도메인 링크 금지 — 실존 사이트로 오인 유도 방지)
    private static final String REVIEW_BASE = "http://localhost:8081/api/wrong-notes/me/due";

    public record Mail(String subject, String html) {}

    private QbitMailTemplate() {
    }

    /** 오답 제출 직후 — 방금 틀린 문제를 오답노트에 담았음을 알린다. */
    public static Mail wrongAnswerNotice(LegacyQuestion question, String userEmail) {
        String subject = "[" + BRAND + "] 오답을 복습 노트에 담았어요 — " + question.title();
        String body = card(
                badge(question.category()),
                esc(question.title()),
                esc(question.content())
        )
                + paragraph("방금 제출한 답이 아쉬웠어요. 이 문제를 <b>오답 노트</b>에 담았습니다. "
                + "QBit은 한 번 틀린 문제를 잊을 때쯤 다시 보여드리는 <b>간격 반복(SM-2)</b> 방식으로 복습을 설계해요.")
                + scheduleBox("내일 다시 출제", "맞히면 복습 간격이 1일 → 3일 → 7일 …로 점점 벌어지고, "
                + "또 틀리면 간격이 1일로 초기화돼 더 자주 만나게 됩니다.")
                + ctaButton("오답 노트에서 복습하기", reviewUrl(userEmail));
        return new Mail(subject, layout("틀린 문제, 그냥 넘기지 않을게요", body));
    }

    /** 매일 아침 — 오늘 복습 기한이 도래한 문제들을 모아 보낸다. */
    public static Mail reviewReminder(List<LegacyQuestion> dueQuestions, String userEmail) {
        String subject = "[" + BRAND + "] 오늘 복습할 문제 " + dueQuestions.size() + "개가 준비됐어요";

        StringBuilder list = new StringBuilder();
        for (LegacyQuestion q : dueQuestions) {
            list.append(card(badge(q.category()), esc(q.title()), esc(q.content())));
        }

        String body = paragraph("좋은 아침이에요 ☀️ 오늘은 복습 간격이 도래한 문제 <b>"
                + dueQuestions.size() + "개</b>가 준비됐어요. 기억이 흐려질 때쯤 다시 보는 것이 "
                + "가장 효율적인 복습 타이밍이에요.")
                + list
                + ctaButton("오늘의 복습 시작하기", reviewUrl(userEmail));
        return new Mail(subject, layout("오늘의 복습 " + dueQuestions.size() + "문제", body));
    }

    private static String reviewUrl(String userEmail) {
        return REVIEW_BASE + "?email=" + URLEncoder.encode(userEmail, StandardCharsets.UTF_8);
    }

    // --- 조립 헬퍼 (인라인 스타일) ---

    private static String layout(String heading, String body) {
        return """
                <div style="margin:0;padding:24px 0;background:#f4f5f7;font-family:-apple-system,'Apple SD Gothic Neo',Segoe UI,Roboto,sans-serif;">
                  <table role="presentation" width="100%%" cellpadding="0" cellspacing="0"><tr><td align="center">
                    <table role="presentation" width="600" cellpadding="0" cellspacing="0" style="max-width:600px;background:#ffffff;border-radius:14px;overflow:hidden;box-shadow:0 1px 4px rgba(0,0,0,.08);">
                      <tr><td style="background:#4338ca;padding:22px 28px;">
                        <span style="color:#ffffff;font-size:22px;font-weight:800;letter-spacing:-.5px;">QBit</span>
                        <span style="color:#c7d2fe;font-size:13px;margin-left:8px;">적응형 기술면접 학습</span>
                      </td></tr>
                      <tr><td style="padding:28px;">
                        <h1 style="margin:0 0 16px;font-size:20px;color:#111827;">%s</h1>
                        %s
                      </td></tr>
                      <tr><td style="padding:18px 28px;background:#fafafa;border-top:1px solid #eee;color:#9ca3af;font-size:12px;line-height:1.6;">
                        이 메일은 QBit 학습 알림으로 발송되었습니다.<br/>
                        원본 매일메일 서비스 종료 이후, 적응형 학습 기능을 직접 구현한 사이드 프로젝트입니다.
                      </td></tr>
                    </table>
                  </td></tr></table>
                </div>
                """.formatted(esc(heading), body);
    }

    private static String card(String badge, String title, String content) {
        return """
                <div style="border:1px solid #e5e7eb;border-radius:10px;padding:18px;margin:14px 0;background:#ffffff;">
                  %s
                  <div style="font-size:16px;font-weight:700;color:#111827;margin:8px 0 6px;">%s</div>
                  <div style="font-size:14px;color:#4b5563;line-height:1.7;">%s</div>
                </div>
                """.formatted(badge, title, content);
    }

    private static String badge(String category) {
        boolean backend = "BACKEND".equalsIgnoreCase(category);
        String bg = backend ? "#eef2ff" : "#ecfdf5";
        String fg = backend ? "#4338ca" : "#047857";
        return "<span style=\"display:inline-block;font-size:11px;font-weight:700;color:" + fg
                + ";background:" + bg + ";padding:3px 10px;border-radius:999px;\">" + esc(category) + "</span>";
    }

    private static String paragraph(String html) {
        return "<p style=\"font-size:14px;color:#374151;line-height:1.8;margin:0 0 12px;\">" + html + "</p>";
    }

    private static String scheduleBox(String when, String desc) {
        return """
                <div style="border-left:4px solid #4338ca;background:#f5f6ff;padding:12px 16px;border-radius:0 8px 8px 0;margin:14px 0;">
                  <div style="font-size:13px;font-weight:700;color:#4338ca;">📅 다음 복습: %s</div>
                  <div style="font-size:13px;color:#4b5563;line-height:1.6;margin-top:4px;">%s</div>
                </div>
                """.formatted(esc(when), esc(desc));
    }

    private static String ctaButton(String label, String url) {
        return "<div style=\"margin:22px 0 4px;\"><a href=\"" + esc(url)
                + "\" style=\"display:inline-block;background:#4338ca;color:#ffffff;text-decoration:none;"
                + "font-size:14px;font-weight:700;padding:12px 22px;border-radius:8px;\">" + esc(label) + " →</a></div>";
    }

    private static String esc(String s) {
        if (s == null) {
            return "";
        }
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;");
    }
}
