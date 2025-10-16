package com.backend.domain.user.util;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisUtil {
    private final StringRedisTemplate redisTemplate;

    /**
     * Redis에 데이터를 저장하면서 만료 시간을 설정합니다
     * @param key 이메일 주소
     * @param value 인증 코드
     * @param duration 만료 시간
     */
    public void setData(String key, String value, long duration) {
        redisTemplate.opsForValue().set(key, value, Duration.ofSeconds(duration));
    }

    /**
     * Redis에서 데이터를 가져옵니다.
     * @param key 이메일 주소
     * @return 저장된 인증 코드
     */
    public String getData(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    /**
     * Redis에서 데이터를 삭제합니다.
     * @param key 이메일 주소
     * @return 삭제 성공 여부
     */
    public Boolean deleteData(String key) {
        return redisTemplate.delete(key);
    }
}
