package com.backend.domain.analysis.lock;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Slf4j
@Component
@Primary
public class RedisLockManager {
    private static final String LOCK_PREFIX = "analysis:lock:";
    private static final long LOCK_TIMEOUT_SECONDS = 300;

    private final StringRedisTemplate redisTemplate;

    public RedisLockManager(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    // 락 획득
    public boolean tryLock(String key){
        String lockKey = LOCK_PREFIX + key;
        String lockValue = Thread.currentThread().getName();

        Boolean success = redisTemplate.opsForValue()
                .setIfAbsent(lockKey, lockValue, Duration.ofSeconds(LOCK_TIMEOUT_SECONDS));

        if(Boolean.TRUE.equals(success)) {
            log.debug("락 획득 성공: {}", key);
            return true;
        }

        log.warn("중복 분석 요청 차단: {}", key);
        return false;
    }

    // 락 갱신
    public void refreshLock(String key) {
        String lockKey = LOCK_PREFIX + key;
        redisTemplate.expire(lockKey, Duration.ofSeconds(LOCK_TIMEOUT_SECONDS));
        log.debug("락 타임 스탬프 갱신: {}", key);
    }

    // 락 해제
    public void releaseLock(String key) {
        String lockKey = LOCK_PREFIX + key;

        redisTemplate.delete(lockKey);
        log.debug("락 해제: {}", key);
    }
}
