package com.backend.domain.user.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {
    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.access-token-expiration-in-milliseconds}")
    private int tokenValidityMilliSeconds;

    //SecretKey를 Base64로 인코딩하여 SecretKey객체로 변환
    private SecretKey key;

    //key값 초기화
    @PostConstruct  //의존성 주입이 될때 딱 1번만 실행되기 때문에 key값은 이후로 변하지 않음
    public void init() {
        this.key = Keys.hmacShaKeyFor(secretKey.getBytes(StandardCharsets.UTF_8));
        System.out.println(key);
    }

    //
    public String createToken(String email, String name) {
        Map<String, Object> claims = new HashMap<>();
        claims.put(Claims.SUBJECT, email); //Claims.SUBJECT = "sub"이다.
        claims.put("name", name);

        Date now = new Date();
        Date expiration = new Date(now.getTime() + tokenValidityMilliSeconds);
        return Jwts.builder()
                .claims(claims)
                .issuedAt(now)
                .expiration(expiration)
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    //JWT 검증 및 Claims 추출
    public Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

    }
}
