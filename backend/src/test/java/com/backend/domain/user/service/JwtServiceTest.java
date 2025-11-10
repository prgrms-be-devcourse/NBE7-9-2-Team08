package com.backend.domain.user.service;

import com.backend.domain.user.repository.UserRepository;
import com.backend.domain.user.util.JwtUtil;
import com.backend.domain.user.util.RedisUtil;
import com.backend.domain.user.util.RefreshTokenUtil;
import lombok.RequiredArgsConstructor;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@RequiredArgsConstructor
public class JwtServiceTest {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    private final RedisUtil redisUtil;
    private final PasswordEncoder passwordEncoder;
    private final RefreshTokenUtil refreshTokenUtil;

    @Test
    @DisplayName("Jwt 생성")
    void t1(){
        //given

        //when

        //then
    }

    @Test
    @DisplayName("refreshToken 생성")
    void t2(){
        //given

        //when

        //then
    }


}
