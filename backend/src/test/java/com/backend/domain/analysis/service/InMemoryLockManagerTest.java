package com.backend.domain.analysis.service;

import com.backend.domain.analysis.lock.InMemoryLockManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

class InMemoryLockManagerTest {

    private InMemoryLockManager lockManager;

    @BeforeEach
    void setUp() {
        lockManager = new InMemoryLockManager();
    }

    @DisplayName("같은 url이 동시에 여러 요청이 들어오면 하나만 락 획득")
    @Test
    void testConcurrentLockAcquisition() throws InterruptedException {
        String key = "user1:https://github.com/test/repo";
        int threadCount = 10;

        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch latch = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        AtomicInteger failCount = new AtomicInteger();

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    if (lockManager.tryLock(key)) {
                        successCount.incrementAndGet();
                    } else {
                        failCount.incrementAndGet();
                    }
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(2, TimeUnit.SECONDS);
        executor.shutdown();

        System.out.printf("성공: %d, 실패: %d%n", successCount.get(), failCount.get());
        assertThat(successCount.get()).isEqualTo(1);
        assertThat(failCount.get()).isEqualTo(9);

        lockManager.releaseLock(key);
        assertThat(lockManager.tryLock(key)).isTrue();
    }

    @DisplayName("서로 다른 url은 동시에 분석이 가능해야 함")
    @Test
    void testDifferentKeysCanLockSimultaneously() {
        String key1 = "user1:repo1";
        String key2 = "user1:repo2";

        boolean lock1 = lockManager.tryLock(key1);
        boolean lock2 = lockManager.tryLock(key2);

        assertThat(lock1).isTrue();
        assertThat(lock2).isTrue();
    }

    @DisplayName("오래된 락 제거")
    @Test
    void testCleanExpiredLocksDoesNotCrash() {
        String key = "user3:repo3";

        lockManager.tryLock(key);
        lockManager.cleanExpiredLocks();

        assertThat(lockManager.tryLock(key)).isFalse();
        lockManager.releaseLock(key);
        assertThat(lockManager.tryLock(key)).isTrue();
    }

    @DisplayName("타임스탬프 갱신")
    @Test
    void testRefreshLockDoesNotThrow() {
        String key = "user4:repo4";

        lockManager.tryLock(key);
        lockManager.refreshLock(key);
        lockManager.releaseLock(key);

        assertThat(lockManager.tryLock(key)).isTrue();
    }
}
