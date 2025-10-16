package com.backend.domain.repository.service.fetcher;

import com.backend.domain.repository.dto.response.github.RepoResponse;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;

@Component
@RequiredArgsConstructor
public class FetcherRepository {

    private final WebClient githubWebClient;

    public RepoResponse fetchRepositoryInfo(String owner, String repoName) {
        try {
            return githubWebClient.get()
                    .uri("/repos/{owner}/{repo}", owner, repoName)
                    .retrieve()
                    .bodyToMono(RepoResponse.class)
                    .onErrorResume(WebClientResponseException.class, ex -> {
                        if (ex.getStatusCode().is4xxClientError()) {
                            return Mono.error(new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND));
                        }
                        if (ex.getStatusCode().is5xxServerError()) {
                            return Mono.error(new BusinessException(ErrorCode.GITHUB_API_SERVER_ERROR));
                        }
                        return Mono.error(ex);
                    })
                    .block();
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.GITHUB_API_FAILED);
        }
    }
}
