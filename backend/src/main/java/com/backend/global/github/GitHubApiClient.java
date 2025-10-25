package com.backend.global.github;

import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.function.Supplier;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubApiClient {

    private final WebClient githubWebClient;

    private static final int RATE_LIMIT_WARNING_THRESHOLD = 100;
    private static final int RATE_LIMIT_CRITICAL_THRESHOLD = 10;

    // GitHub API GET 요청 - 기본 JSON 응답
    public <T> T get(String uri, Class<T> responseType, Object... uriVariables) {
        return executeRequest(() ->
                githubWebClient.get()
                        .uri(uri, uriVariables)
                        .retrieve()
                        .toEntity(responseType)
                        .map(response -> {
                            checkRateLimit(response.getHeaders());
                            return response.getBody();
                        })
        );
    }

    // GitHub API GET 요청 - 커스텀 Accept 헤더
    public <T> T getWithAcceptHeader(String uri, Class<T> responseType, String acceptHeader, Object... uriVariables) {
        return executeRequest(() ->
                githubWebClient.get()
                        .uri(uri, uriVariables)
                        .header("Accept", acceptHeader)
                        .retrieve()
                        .toEntity(responseType)
                        .map(response -> {
                            checkRateLimit(response.getHeaders());
                            return response.getBody();
                        })
        );
    }

    // GitHub API GET 요청 - Base64 디코딩 (Readme)
    public String getRaw(String uri, Object... uriVariables) {
        return executeRequest(() ->
                githubWebClient.get()
                        .uri(uri, uriVariables)
                        .header("Accept", "application/vnd.github.raw")
                        .retrieve()
                        .toEntity(String.class)
                        .map(response -> {
                            checkRateLimit(response.getHeaders());
                            String body = response.getBody();
                            return (body != null) ? body : "";
                        })
        );
    }

    // GitHub API List 응답 처리 - 배열 형태 데이터
    public <T> List<T> getList(String uri, Class<T> elementType, Object... uriVariables) {
        return executeRequest(() ->
                githubWebClient.get()
                        .uri(uri, uriVariables)
                        .retrieve()
                        .toEntityList(elementType)
                        .map(response -> {
                            checkRateLimit(response.getHeaders());
                            return response.getBody();
                        })
        );
    }

    // GitHub API 요청 실행 및 공통 예외 처리
    private <T> T executeRequest(Supplier<Mono<T>> requestSupplier) {
        return requestSupplier.get()
                .onErrorResume(WebClientResponseException.class, this::handleWebClientError)
                .block();
    }

    // WebClient 응답 예외를 비즈니스 예외로 변환
    private <T> Mono<T> handleWebClientError(WebClientResponseException ex) {
        log.error("GitHub API 호출 실패: {}", ex.getMessage());

        if (ex.getStatusCode().is4xxClientError()) {
            HttpStatus status = (HttpStatus) ex.getStatusCode();

            if (status == HttpStatus.BAD_REQUEST) {
                return Mono.error(new BusinessException(ErrorCode.GITHUB_API_FAILED));
            }
            if (status == HttpStatus.UNAUTHORIZED) {
                return Mono.error(new BusinessException(ErrorCode.GITHUB_INVALID_TOKEN));
            }
            if (status == HttpStatus.FORBIDDEN) {
                return Mono.error(new BusinessException(ErrorCode.FORBIDDEN));
            }
            if (status == HttpStatus.NOT_FOUND) {
                return Mono.error(new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND));
            }
            if (status == HttpStatus.GONE) {
                return Mono.error(new BusinessException(ErrorCode.GITHUB_API_FAILED));
            }
            if (status == HttpStatus.UNPROCESSABLE_ENTITY) {
                return Mono.error(new BusinessException(ErrorCode.GITHUB_API_FAILED));
            }
            if (status == HttpStatus.TOO_MANY_REQUESTS) {
                return Mono.error(new BusinessException(ErrorCode.GITHUB_RATE_LIMIT_EXCEEDED));
            }

            return Mono.error(new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND));
        }
        if (ex.getStatusCode().is5xxServerError()) {
            return Mono.error(new BusinessException(ErrorCode.GITHUB_API_SERVER_ERROR));
        }
        return Mono.error(new BusinessException(ErrorCode.GITHUB_API_FAILED));
    }

    // GitHub API Rate Limit 상태 확인 및 로깅
    private void checkRateLimit(HttpHeaders headers) {
        try {
            String remainingStr = getHeaderValue(headers, "X-RateLimit-Remaining");
            String resetStr = getHeaderValue(headers, "X-RateLimit-Reset");

            if (remainingStr == null || resetStr == null) {
                log.debug("Rate Limit 헤더를 찾을 수 없습니다.");
                return;
            }

            int remaining = Integer.parseInt(remainingStr);
            long resetTime = Long.parseLong(resetStr);
            long currentTime = System.currentTimeMillis() / 1000;
            long timeUntilReset = resetTime - currentTime;

            log.info("GitHub API Rate Limit - 남은 요청: {}, 초기화까지: {}분",
                    remaining, timeUntilReset / 60);

            if (remaining <= RATE_LIMIT_CRITICAL_THRESHOLD) {
                log.error("🚨 GitHub API Rate Limit - 남은 요청: {}, 초기화: {}분 후",
                        remaining, timeUntilReset / 60);
            } else if (remaining <= RATE_LIMIT_WARNING_THRESHOLD) {
                log.warn("⚠️ GitHub API Rate Limit - 남은 요청: {}, 초기화: {}분 후",
                        remaining, timeUntilReset / 60);
            }

        } catch (NumberFormatException e) {
            log.error("Rate Limit 헤더 값 파싱 실패", e);
        } catch (Exception e) {
            log.error("Rate Limit 헤더 처리 중 오류 발생", e);
        }
    }

    // HTTP 헤더에서 특정 값 추출
    private String getHeaderValue(HttpHeaders headers, String headerName) {
        List<String> values = headers.get(headerName);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
}
