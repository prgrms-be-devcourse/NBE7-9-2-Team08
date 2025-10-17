package com.backend.domain.repository.service.fetcher;

import com.backend.domain.repository.dto.response.github.CommitResponse;
import com.backend.domain.repository.dto.response.github.RepoResponse;
import com.backend.domain.repository.dto.response.github.TreeResponse;
import com.backend.global.github.GitHubApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class GitHubDataFetcher {
    private final GitHubApiClient gitHubApiClient;

    public RepoResponse fetchRepositoryInfo(String owner, String repoName) {
        return gitHubApiClient.get("/repos/{owner}/{repo}", RepoResponse.class, owner, repoName);
    }

    public String fetchReadmeContent(String owner, String repoName) {
        return gitHubApiClient.getRaw("/repos/{owner}/{repo}/readme", owner, repoName);
    }

    public List<CommitResponse> fetchCommitInfo(String owner, String repoName, String since) {
        return gitHubApiClient.getList(
                "/repos/{owner}/{repo}/commits?since=" + since + "&per_page=100", CommitResponse.class, owner, repoName);
    }

    public TreeResponse fetchRepositoryTreeInfo(String owner, String repoName, String defaultBranch) {
        return gitHubApiClient.get(
                "/repos/{owner}/{repo}/git/trees/{sha}?recursive=1", TreeResponse.class, owner, repoName, defaultBranch
        );
    }
}
