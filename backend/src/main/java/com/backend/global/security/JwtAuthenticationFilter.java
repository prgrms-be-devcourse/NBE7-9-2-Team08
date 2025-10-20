package com.backend.global.security;

import com.backend.domain.user.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;


    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String authorizationHeader = request.getHeader("Authorization");
        String token = null;

        //"Bearer " 제거
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            token = authorizationHeader.substring(7);
        }

        //토큰이 있다면 검증 및 인증
        if(token != null) {
            try {
                Claims claims = jwtUtil.parseClaims(token);

                String email = claims.getSubject(); //"sub"값 가져오기

                //추출된 정보로 Spring Security 인증 객체 생성 (파싱)
                Authentication authentication = null;
                if (email != null) {
                    //나중에 권한을 추가하면 Collections.emptyList()대신 넣을것
                    authentication = new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            Collections.emptyList());
                }

                // SecurityContextHolder에 인증 정보 저장
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }catch (ExpiredJwtException e) {
                // 토큰 만료 시
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token Expired : 토큰의 유효기간이 지났습니다.");
                return;
            } catch (JwtException e) {
                // 서명 불일치, 토큰 형식 오류 등
                sendErrorResponse(response, HttpServletResponse.SC_FORBIDDEN, "Invalid Token : 올바른 토큰 값이 아닙니다.");
                return;
            }

        }
        // 다음 필터로 요청 전달
        filterChain.doFilter(request, response);
    }

    private void sendErrorResponse(HttpServletResponse response, int status, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status);

        Map<String, Object> errorDetails = new HashMap<>();
        errorDetails.put("status", status);
        errorDetails.put("error", "Authentication Failed");
        errorDetails.put("message", message);
        errorDetails.put("timestamp", LocalDateTime.now().toString());

        //Map을 JSON 문자열로 변환하여 응답 본문에 작성
        ObjectMapper objectMapper = new ObjectMapper();
        response.getWriter().write(objectMapper.writeValueAsString(errorDetails));
    }
}
