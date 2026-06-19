package maeilmail.learning.config;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.env.EnvironmentPostProcessor;
import org.springframework.core.Ordered;
import org.springframework.core.env.ConfigurableEnvironment;
import org.springframework.core.env.MapPropertySource;

/**
 * 프로젝트 루트의 {@code .env} 파일을 Spring Environment로 직접 로드한다.
 *
 * <p>셸에서 {@code source .env}를 하지 않아도(또는 Gradle 데몬이 환경변수를 앱에
 * 전달하지 못해도) {@code SMTP_USERNAME} 같은 값이 앱에 도달하도록 보장한다.
 * 우선순위는 가장 낮게(addLast) 두어, 실제 OS 환경변수나 커맨드라인 값이 있으면 그쪽이 이긴다.
 *
 * <p>local / dev 프로필일 때만 동작한다. 테스트는 .env를 읽지 않으므로 Mock 발송기를 유지한다.
 */
public class DotenvEnvironmentPostProcessor implements EnvironmentPostProcessor, Ordered {

    @Override
    public void postProcessEnvironment(ConfigurableEnvironment environment, SpringApplication application) {
        String profiles = environment.getProperty("spring.profiles.active", "");
        if (!profiles.contains("local") && !profiles.contains("dev")) {
            return; // 테스트 등에서는 .env를 읽지 않는다
        }
        File dotenv = locate();
        if (dotenv == null) {
            return;
        }
        Map<String, Object> values = parse(dotenv);
        if (!values.isEmpty()) {
            environment.getPropertySources().addLast(new MapPropertySource("dotenv", values));
            System.out.printf("📄 .env 로드됨: %s (%d개 값)%n", dotenv.getAbsolutePath(), values.size());
        }
    }

    // 실행 디렉터리(모듈 또는 루트)에서 위로 올라가며 .env를 찾는다.
    private File locate() {
        File dir = new File(System.getProperty("user.dir"));
        for (int i = 0; i < 4 && dir != null; i++, dir = dir.getParentFile()) {
            File candidate = new File(dir, ".env");
            if (candidate.isFile()) {
                return candidate;
            }
        }
        return null;
    }

    private Map<String, Object> parse(File file) {
        Map<String, Object> map = new HashMap<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(file, StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                int eq = line.indexOf('=');
                if (eq <= 0) {
                    continue;
                }
                String key = line.substring(0, eq).trim();
                String value = stripQuotes(line.substring(eq + 1).trim());
                map.put(key, value);
            }
        } catch (IOException ignored) {
            // .env 읽기 실패는 무시 — 환경변수 폴백
        }
        return map;
    }

    private String stripQuotes(String v) {
        if (v.length() >= 2
                && ((v.startsWith("\"") && v.endsWith("\"")) || (v.startsWith("'") && v.endsWith("'")))) {
            return v.substring(1, v.length() - 1);
        }
        return v;
    }

    @Override
    public int getOrder() {
        return Ordered.LOWEST_PRECEDENCE;
    }
}
