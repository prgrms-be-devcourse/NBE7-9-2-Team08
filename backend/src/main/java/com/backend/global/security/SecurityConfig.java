package com.backend.global.security;

import com.backend.domain.user.service.JwtService;
import com.backend.domain.user.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {
    private final JwtUtil jwtUtil;
    private final JwtService jwtService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                // JWT 인증을 사용하므로 세션을 사용하지 않음 (Stateless)
                .sessionManagement(session -> session
                        .sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // H2 콘솔은 frameOptions 해제 필요 (iframe으로 동작)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                )

                // CSRF 공격 방지 비활성화 (Stateless API에서 주로 사용)
                // H2 콘솔은 CSRF 예외로 설정
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/login", //로그인
                                "/api/auth",  //이메인 인증코드 전송
                                "/api/verify",//이메일 인증코드 검증
                                "/api/user",  //회원가입
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/h2-console/**" // H2 콘솔 허용
                        ).permitAll()
                        .requestMatchers(
                                "/api/analysis/**",
                                "/api/repositories/**",
                                "/api/ai/complete/**",
                                "/api/community/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable())
                //커스텀 JWT 필터를 등록
                .addFilterBefore(new JwtAuthenticationFilter(jwtUtil, jwtService),
                        UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
