package com.backend.global.security;

import com.backend.domain.user.service.JwtService;
import com.backend.domain.user.util.JwtUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtil jwtUtil;
    private final JwtService jwtService;

    private record ExcludedRequest(String path, String method) {}

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        String requestURI = request.getRequestURI();
        String method = request.getMethod();

        //  JWT 검증이 필요 없는 URL (회원가입, 로그인, 이메일 인증코드 발송,이메일 인증코드 검증)
        List<ExcludedRequest> excludedRequests = List.of(
                // 개발 도구 (모든 메서드 허용)
                new ExcludedRequest("/h2-console/**", null),
                new ExcludedRequest("/swagger-ui/**", null),
                new ExcludedRequest("/v3/api-docs/**", null),
                new ExcludedRequest("/swagger-resources/**", null),
                new ExcludedRequest("/webjars/**", null),

                // 인증 관련 API
                new ExcludedRequest("/api/login", "POST"),
                new ExcludedRequest("/api/auth", "POST"),
                new ExcludedRequest("/api/verify", "POST"),
                new ExcludedRequest("/api/user", "POST"),

                // 커뮤니티 관련 API
                new ExcludedRequest("/api/community/**", null),

                new ExcludedRequest("/api/analysis/**", null),
                new ExcludedRequest("/api/repositories/**", null),
                new ExcludedRequest("/api/ai/complete/**", null)
        );

        // 요청 경로 + 메서드가 일치하는 경우 필터 스킵
        AntPathMatcher pathMatcher = new AntPathMatcher();

        boolean excluded = excludedRequests.stream()
                .anyMatch(ex -> {
                    //요청 url이 List의 url과 일치하는지 확인(/**도 지원)
                    boolean pathMatches = pathMatcher.match(ex.path(), requestURI);

                    // 메서드가 null (모든 메서드)이거나, 메서드가 일치하는 경우
                    boolean methodMatches = ex.method() == null || ex.method().equalsIgnoreCase(method);

                    //두 조건이 true일때 JWT인증을 스킵
                    return pathMatches && methodMatches;
                });

        if (excluded) {
            filterChain.doFilter(request, response);
            return;
        }

        //
        

        String token = extractTokenFromCookie(request);

/*        //"Bearer " 제거
        if(authorizationHeader != null && authorizationHeader.startsWith("Bearer ")){
            token = authorizationHeader.substring(7);
        }
        */

        //token이 null이거나 비어있다면 JWT가 입력되지 않은것으로 판단
        if(token==null||token.isEmpty()){
            sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token error : JWT가 입력되지 않았습니다.");
            return;
        }

        //토큰이 있다면 검증 및 인증
        if(token != null) {
            //블랙리스트를 조회하여 토큰이 무효화 되었는지 확인
            if(jwtService.isBlacklisted(token)){
                sendErrorResponse(response, HttpServletResponse.SC_UNAUTHORIZED, "Token Expired : 토큰이 무효되어 있습니다.(로그아웃 상태입니다.)");
                return; //요청 차단
            }
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

    private String extractTokenFromCookie(HttpServletRequest request) {
        if(request.getCookies()==null){
            return null;
        }
        for(Cookie cookie : request.getCookies()){
            if(cookie.getName().equals("token")){
                String value = cookie.getValue();
                if (value != null && value.startsWith("Bearer%20")) {
                    return value.substring(9);
                } else if (value != null && value.startsWith("Bearer ")) {
                    return value.substring(7);
                }
                return value;
            }
        }
        return null;
    }

    //에러 발생시 메세지 처리
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
