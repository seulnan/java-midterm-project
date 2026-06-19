package maeilmail.learning.infrastructure.mail;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import maeilmail.learning.adapter.LegacyQuestion;
import maeilmail.learning.domain.grading.Concept;
import maeilmail.learning.domain.grading.GradeResult;

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
    // 질문 메일의 "답안 작성" 버튼이 가리키는 답안 페이지(정적 리소스).
    private static final String ANSWER_BASE = "http://localhost:8081/answer.html";

    public record Mail(String subject, String html) {}

    private QbitMailTemplate() {
    }

    /** 오늘의 질문 — 사용자에게 면접 질문을 보내고, 메일 안에서 답안 페이지로 연결한다. */
    public static Mail questionDelivery(LegacyQuestion question, String userEmail) {
        String subject = "[" + BRAND + "] 오늘의 면접 질문 — " + question.title();
        String body = paragraph("오늘의 질문이 도착했어요. 아래 문제를 보고 <b>버튼을 눌러 답안을 작성</b>하면, "
                + "QBit이 채점하고 결과를 다시 메일로 보내드립니다.")
                + card(badge(question.category()), esc(question.title()), esc(question.content()))
                + ctaButton("답안 작성하기", answerUrl(question.id(), userEmail));
        return new Mail(subject, layout("오늘의 면접 질문", body));
    }

    /**
     * 답안 피드백 — 채점 결과(점수·짚은 개념·놓친 개념·모범답안)를 담는다.
     * 정답이면 칭찬 + 난이도 안내, 오답이면 약점(놓친 개념 + 왜)과 복습 주기 근거를 보여준다.
     */
    public static Mail answerFeedback(LegacyQuestion question, GradeResult grade, String userEmail) {
        boolean correct = grade.correct();
        int total = grade.matched().size() + grade.missed().size();
        String subject = "[" + BRAND + "] " + (correct ? "정답이에요! " : "오답 피드백 ")
                + "(" + grade.score() + "점) — " + question.title();

        StringBuilder body = new StringBuilder();
        body.append(card(badge(question.category()), esc(question.title()), esc(question.content())));
        body.append(scoreLine(grade.score(), correct, grade.matched().size(), total));

        if (!grade.matched().isEmpty()) {
            body.append(paragraph("<b>잘 짚은 개념:</b> " + esc(String.join(", ", grade.matched()))));
        }
        if (!correct && !grade.missed().isEmpty()) {
            body.append(missedBlock(grade.missed()));
        }
        if (grade.modelAnswer() != null) {
            body.append(modelBox(grade.modelAnswer()));
        }

        if (correct) {
            body.append(noticeBox("#047857", "#ecfdf5", "✓ 정답 처리 완료",
                    "이 문제는 오답 노트에 담지 않았어요. 정답률이 오르면 다음 문제의 난이도가 한 단계 올라갑니다."));
        } else {
            body.append(scheduleBox("내일 다시 출제 (1일 뒤)",
                    "처음 틀렸으니 가장 짧은 주기로 다시 만나요. 복습에서 맞히면 간격이 1일 → 3일 → 7일로 벌어지고, "
                    + "또 틀리면 1일로 초기화돼 더 자주 보게 됩니다."));
            body.append(ctaButton("오답 노트에서 복습하기", reviewUrl(userEmail)));
        }

        String heading = correct ? "정답입니다 ✓" : "어떤 개념이 부족했는지 짚어봤어요";
        return new Mail(subject, layout(heading, body.toString()));
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

    private static String answerUrl(Long questionId, String userEmail) {
        return ANSWER_BASE + "?questionId=" + questionId
                + "&email=" + URLEncoder.encode(userEmail, StandardCharsets.UTF_8);
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

    private static String scoreLine(int score, boolean correct, int matched, int total) {
        String color = correct ? "#047857" : "#b91c1c";
        String bg = correct ? "#ecfdf5" : "#fef2f2";
        return "<div style=\"background:" + bg + ";border-radius:10px;padding:14px 16px;margin:14px 0;\">"
                + "<span style=\"font-size:24px;font-weight:800;color:" + color + ";\">" + score + "점</span>"
                + "<span style=\"font-size:13px;color:#6b7280;margin-left:10px;\">핵심 개념 " + total + "개 중 "
                + matched + "개 충족</span></div>";
    }

    private static String missedBlock(List<Concept> missed) {
        StringBuilder sb = new StringBuilder();
        sb.append("<div style=\"font-size:14px;font-weight:700;color:#b91c1c;margin:18px 0 6px;\">놓친 개념 (약점)</div>");
        for (Concept c : missed) {
            sb.append("<div style=\"border-left:3px solid #fca5a5;padding:8px 14px;margin:8px 0;\">"
                    + "<div style=\"font-size:14px;font-weight:700;color:#111827;\">✗ " + esc(c.label()) + "</div>"
                    + "<div style=\"font-size:13px;color:#4b5563;line-height:1.6;margin-top:3px;\">" + esc(c.why()) + "</div>"
                    + "</div>");
        }
        return sb.toString();
    }

    private static String noticeBox(String fg, String bg, String title, String desc) {
        return "<div style=\"background:" + bg + ";border-radius:8px;padding:12px 16px;margin:14px 0;\">"
                + "<div style=\"font-size:13px;font-weight:700;color:" + fg + ";\">" + esc(title) + "</div>"
                + "<div style=\"font-size:13px;color:#4b5563;line-height:1.6;margin-top:4px;\">" + esc(desc) + "</div></div>";
    }

    private static String modelBox(String modelAnswer) {
        return "<div style=\"background:#f5f6ff;border-radius:10px;padding:14px 16px;margin:14px 0;\">"
                + "<div style=\"font-size:13px;font-weight:700;color:#4338ca;margin-bottom:4px;\">모범답안 요지</div>"
                + "<div style=\"font-size:13px;color:#374151;line-height:1.7;\">" + esc(modelAnswer) + "</div></div>";
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
