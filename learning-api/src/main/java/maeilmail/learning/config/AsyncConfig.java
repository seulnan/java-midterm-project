package maeilmail.learning.config;

import java.util.concurrent.Executor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.aop.interceptor.AsyncUncaughtExceptionHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.AsyncConfigurer;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

@Slf4j
@Configuration
class AsyncConfig implements AsyncConfigurer {

    @Override
    public AsyncUncaughtExceptionHandler getAsyncUncaughtExceptionHandler() {
        return (ex, method, params) ->
                log.error("비동기 처리 실패 method={} params={}", method.getName(), params, ex);
    }

    @Bean("mailExecutor")
    public Executor mailExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(3);
        executor.setMaxPoolSize(10);
        executor.setQueueCapacity(100);
        executor.setThreadNamePrefix("mail-");
        executor.setRejectedExecutionHandler((r, pool) ->
                log.warn("mailExecutor 큐 포화 — 메일 태스크 드롭: {}", r));
        executor.initialize();
        return executor;
    }

    @Bean("statExecutor")
    public Executor statExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(2);
        executor.setMaxPoolSize(4);
        executor.setQueueCapacity(200);
        executor.setThreadNamePrefix("stat-");
        executor.setRejectedExecutionHandler((r, pool) ->
                log.warn("statExecutor 큐 포화 — 통계 태스크 드롭: {}", r));
        executor.initialize();
        return executor;
    }
}
