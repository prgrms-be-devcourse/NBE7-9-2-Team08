package com.backend.global.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())

                // H2 콘솔은 frameOptions 해제 필요 (iframe으로 동작)
                .headers(headers -> headers
                        .frameOptions(frame -> frame.disable())
                )
                // H2 콘솔은 CSRF 예외로 설정
                .csrf(csrf -> csrf.disable())

                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/**",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-resources/**",
                                "/webjars/**",
                                "/h2-console/**" // H2 콘솔 허용
                        ).permitAll()
                        .requestMatchers(
                                "/api/analysis/**",
                                "/api/repositories/**"
                        ).permitAll()
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}
