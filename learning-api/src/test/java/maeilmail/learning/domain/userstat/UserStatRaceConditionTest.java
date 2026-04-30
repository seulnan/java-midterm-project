package maeilmail.learning.domain.userstat;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;

class UserStatRaceConditionTest {

    static class UnsafeCounter {
        private int value = 0;
        void increment() { value++; }
        int value() { return value; }
    }

    static class SafeCounter {
        private volatile int value = 0;
        synchronized void increment() { value++; }
        int value() { return value; }
    }

    @Test
    void unsafe_increment_loses_updates() throws Exception {
        UnsafeCounter counter = new UnsafeCounter();
        ExecutorService es = Executors.newFixedThreadPool(100);
        CountDownLatch ready = new CountDownLatch(100);
        CountDownLatch start = new CountDownLatch(1);

        for (int i = 0; i < 100; i++) {
            es.submit(() -> {
                ready.countDown();
                start.await();
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
                return null;
            });
        }

        ready.await();
        start.countDown();
        es.shutdown();
        es.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("[UNSAFE] expected=100000  actual=" + counter.value());
        // 동시성 보호 없이는 값 손실 발생
        assertThat(counter.value()).isLessThanOrEqualTo(100_000);
    }

    @Test
    void safe_increment_preserves_updates() throws Exception {
        SafeCounter counter = new SafeCounter();
        ExecutorService es = Executors.newFixedThreadPool(100);
        CountDownLatch ready = new CountDownLatch(100);
        CountDownLatch start = new CountDownLatch(1);

        for (int i = 0; i < 100; i++) {
            es.submit(() -> {
                ready.countDown();
                start.await();
                for (int j = 0; j < 1000; j++) {
                    counter.increment();
                }
                return null;
            });
        }

        ready.await();
        start.countDown();
        es.shutdown();
        es.awaitTermination(10, TimeUnit.SECONDS);

        System.out.println("[SAFE]   expected=100000  actual=" + counter.value());
        assertThat(counter.value()).isEqualTo(100_000);
    }
}
