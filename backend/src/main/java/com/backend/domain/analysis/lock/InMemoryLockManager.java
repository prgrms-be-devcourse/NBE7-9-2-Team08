package com.backend.domain.analysis.lock;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Component
public class InMemoryLockManager {
    private static final long LOCK_TIMEOUT_MILLIS = 5 * 60 * 1000; // 락 유지 시간 5분

    // 현재 진행 중인 분석 정보
    private final ConcurrentHashMap<String, LockInfo> processingRepositories = new ConcurrentHashMap<>();

    // 만료된 락 정리
    @Scheduled(fixedDelay = 60000)
    public void cleanExpiredLocks() {
        processingRepositories.entrySet().removeIf(entry ->
                entry.getValue().isExpired(LOCK_TIMEOUT_MILLIS)); // 만료된 락만 정리
    }

    // 락 획득
    public boolean tryLock(String key) {
        LockInfo newLock = new LockInfo(System.currentTimeMillis(), Thread.currentThread().getName());
        LockInfo existing = processingRepositories.putIfAbsent(key, newLock);

        if (existing != null && !existing.isExpired(LOCK_TIMEOUT_MILLIS)) {
            log.warn("중복 분석 요청 차단: {}", key);
            return false;
        }

        if (existing != null && existing.isExpired(LOCK_TIMEOUT_MILLIS)) {
            processingRepositories.put(key, newLock);
            log.info("만료된 락 재획득: {}", key);
        }

        log.debug("락 획득 성공: {}", key);
        return true;
    }

    // 락 타임스탬프 갱신
    public void refreshLock(String key) {
        processingRepositories.computeIfPresent(key, (k, old) ->
                new LockInfo(System.currentTimeMillis(), old.getOwner())
        );
        log.debug("락 타임스탬프 갱신: {}", key);
    }

    // 락 해제
    public void releaseLock(String key) {
        processingRepositories.remove(key);
        log.debug("락 해제: {}", key);
    }

    @Getter
    private static class LockInfo {
        private final long timestamp;
        private final String owner;

        public LockInfo(long timestamp, String owner) {
            this.timestamp = timestamp;
            this.owner = owner;
        }

        public boolean isExpired(long timeoutMillis) {
            return System.currentTimeMillis() - timestamp > timeoutMillis;
        }
    }
}
