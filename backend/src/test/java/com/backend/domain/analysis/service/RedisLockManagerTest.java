package com.backend.domain.analysis.service;

import com.backend.domain.analysis.lock.RedisLockManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class RedisLockManagerTest {

    @Autowired
    private RedisLockManager lockManager;

    @Autowired
    private StringRedisTemplate redisTemplate;

    @BeforeEach
    void setUp() {
        // 테스트 전 Redis 초기화
        redisTemplate.getConnectionFactory().getConnection().flushDb();
    }

    @AfterEach
    void tearDown() {
        // 테스트 후 Redis 정리
        redisTemplate.getConnectionFactory().getConnection().flushDb();
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

    @DisplayName("Redis TTL로 자동 만료 확인")
    @Test
    void testLockAutoExpiration() throws InterruptedException {
        String key = "user3:repo3";

        // 락 획득
        assertThat(lockManager.tryLock(key)).isTrue();

        // 동일 키로 재시도 - 실패
        assertThat(lockManager.tryLock(key)).isFalse();

        // Redis TTL 확인 (약 300초 남아있어야 함)
        Long ttl = redisTemplate.getExpire("analysis:lock:" + key, TimeUnit.SECONDS);
        assertThat(ttl).isBetween(295L, 300L);
    }

    @DisplayName("타임스탬프 갱신")
    @Test
    void testRefreshLock() throws InterruptedException {
        String key = "user4:repo4";

        lockManager.tryLock(key);

        // 잠시 대기 후 TTL 감소 확인
        Thread.sleep(3000);
        Long ttlBefore = redisTemplate.getExpire("analysis:lock:" + key, TimeUnit.SECONDS);

        // 락 갱신
        lockManager.refreshLock(key);
        Long ttlAfter = redisTemplate.getExpire("analysis:lock:" + key, TimeUnit.SECONDS);

        // 갱신 후 TTL이 다시 늘어났는지 확인
        assertThat(ttlAfter).isGreaterThan(ttlBefore);
        assertThat(ttlAfter).isBetween(295L, 300L);
    }

    @DisplayName("락 해제 후 재획득 가능")
    @Test
    void testLockReleaseAndReacquire() {
        String key = "user5:repo5";

        // 첫 번째 락 획득
        assertThat(lockManager.tryLock(key)).isTrue();

        // 해제 전 재시도 - 실패
        assertThat(lockManager.tryLock(key)).isFalse();

        // 락 해제
        lockManager.releaseLock(key);

        // 재획득 - 성공
        assertThat(lockManager.tryLock(key)).isTrue();
    }
}