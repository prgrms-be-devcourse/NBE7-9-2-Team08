package com.backend.global.github;

import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
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

    private <T> T executeRequest(Supplier<Mono<T>> requestSupplier) {
        return requestSupplier.get()
                .onErrorResume(WebClientResponseException.class, this::handleWebClientError)
                .block();
    }

    private <T> Mono<T> handleWebClientError(WebClientResponseException ex) {
        log.error("GitHub API 호출 실패: {}", ex.getMessage());

        if (ex.getStatusCode().is4xxClientError()) {
            return Mono.error(new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND));
        }
        if (ex.getStatusCode().is5xxServerError()) {
            return Mono.error(new BusinessException(ErrorCode.GITHUB_API_SERVER_ERROR));
        }
        return Mono.error(new BusinessException(ErrorCode.GITHUB_API_FAILED));
    }

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

    private String getHeaderValue(HttpHeaders headers, String headerName) {
        List<String> values = headers.get(headerName);
        return (values != null && !values.isEmpty()) ? values.get(0) : null;
    }
}
