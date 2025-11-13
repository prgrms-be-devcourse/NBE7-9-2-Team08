package com.backend.domain.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class RefreshTokenUtil {
    private final RedisUtil redisUtil;

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.refresh-token-expiration-in-milliseconds}")
    private long refreshTokenMilliSeconds;

    //SecretKey를 Base64로 인코딩하여 SecretKey객체로 변환
    private SecretKey key;

    //key값 초기화
    @PostConstruct  //의존성 주입이 될때 딱 1번만 실행되기 때문에 key값은 이후로 변하지 않음
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        System.out.println(key);
    }

    public String createToken(Long userId) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Claims.SUBJECT, userId.toString()); // Claims.SUBJECT = "sub"이다.
        String jti = UUID.randomUUID().toString();
        claims.put(Claims.ID, jti); //Claims.ID = "jti"

        Date now = new Date();
        System.out.println("현재 시간 : " + now.toString());
        Date expiration = new Date(now.getTime() + refreshTokenMilliSeconds);
        System.out.println("만료 시간 : " + expiration.toString());

        String refreshToken = Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();

        redisUtil.setData(jti, refreshToken,  refreshTokenMilliSeconds/1000);

        return refreshToken;
    }
}
