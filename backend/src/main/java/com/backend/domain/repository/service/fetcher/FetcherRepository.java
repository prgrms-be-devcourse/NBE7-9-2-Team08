package com.backend.domain.repository.service.fetcher;

import com.backend.domain.repository.dto.response.github.RepoResponse;
import com.backend.global.github.GitHubApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class FetcherRepository {
    private final GitHubApiClient gitHubApiClient;

    public RepoResponse fetchRepositoryInfo(String owner, String repoName) {
        return gitHubApiClient.get("/repos/{owner}/{repo}", RepoResponse.class, owner, repoName);
    }
}
