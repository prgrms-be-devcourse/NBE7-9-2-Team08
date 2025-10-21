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

    /**
     * Redis에서 데이터가 있는지 확인만 합니다.
     * 값을 네트로워크로 전송하지 않아서 get()보다 오버헤드가 작습니다.
     *
     */
    public Boolean hasKey(String key) {
        return redisTemplate.hasKey(key);
    }
}
