package com.backend.domain.repository.service.fetcher;

import com.backend.domain.repository.dto.response.github.*;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import com.backend.global.github.GitHubApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
@Component
@RequiredArgsConstructor
public class GitHubDataFetcher {
    private final GitHubApiClient gitHubApiClient;
    private static final int COMMUNITY_ANALYSIS_MONTHS = 6;

    @Retryable(
            retryFor = {WebClientResponseException.ServiceUnavailable.class,
                    WebClientResponseException.InternalServerError.class,
                    WebClientRequestException.class},  // 네트워크 타임아웃
            maxAttempts = 2,  // 최대 2회 시도 (원본 1회 + 재시도 1회)
            backoff = @Backoff(delay = 1000)  // 재시도 전 1초 대기
    )
    public RepoResponse fetchRepositoryInfo(String owner, String repoName) {
        return gitHubApiClient.get("/repos/{owner}/{repo}", RepoResponse.class, owner, repoName);
    }

    @Retryable(
            retryFor = {WebClientResponseException.ServiceUnavailable.class,
                    WebClientResponseException.InternalServerError.class,
                    WebClientRequestException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public Optional<String> fetchReadmeContent(String owner, String repoName) {
        try {
            String content = gitHubApiClient.getRaw("/repos/{owner}/{repo}/readme", owner, repoName);

            if (content == null || content.trim().isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(content);
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.GITHUB_REPO_NOT_FOUND) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Retryable(
            retryFor = {WebClientResponseException.ServiceUnavailable.class,
                    WebClientResponseException.InternalServerError.class,
                    WebClientRequestException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public List<CommitResponse> fetchCommitInfo(String owner, String repoName, String since) {
        try {
            return gitHubApiClient.getList(
                    "/repos/{owner}/{repo}/commits?since={since}&per_page=100",
                    CommitResponse.class, owner, repoName, since
            );
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.GITHUB_REPO_NOT_FOUND) {
                return Collections.emptyList();
            }
            throw e;
        }
    }

    @Retryable(
            retryFor = {WebClientResponseException.ServiceUnavailable.class,
                    WebClientResponseException.InternalServerError.class,
                    WebClientRequestException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public Optional<TreeResponse> fetchRepositoryTreeInfo(String owner, String repoName, String defaultBranch) {
        try {
            TreeResponse tree = gitHubApiClient.get(
                    "/repos/{owner}/{repo}/git/trees/{sha}?recursive=1",
                    TreeResponse.class, owner, repoName, defaultBranch
            );

            if (tree == null || tree.tree() == null || tree.tree().isEmpty()) {
                return Optional.empty();
            }

            return Optional.of(tree);
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.GITHUB_REPO_NOT_FOUND) {
                return Optional.empty();
            }
            throw e;
        }
    }

    @Retryable(
            retryFor = {WebClientResponseException.ServiceUnavailable.class,
                    WebClientResponseException.InternalServerError.class,
                    WebClientRequestException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public List<IssueResponse> fetchIssueInfo(String owner, String repoName) {
        try {
            List<IssueResponse> allIssues = gitHubApiClient.getList(
                    "/repos/{owner}/{repo}/issues?state=all&per_page=100",
                    IssueResponse.class, owner, repoName
            );

            LocalDateTime sixMonthsAgo = getSixMonthsAgo();
            return allIssues.stream()
                    .filter(IssueResponse::isPureIssue)
                    .filter(issue -> parseGitHubDate(issue.created_at()).isAfter(sixMonthsAgo))
                    .collect(Collectors.toList());

        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.GITHUB_REPO_NOT_FOUND) {
                return Collections.emptyList();
            }
            throw e;
        }
    }

    @Retryable(
            retryFor = {WebClientResponseException.ServiceUnavailable.class,
                    WebClientResponseException.InternalServerError.class,
                    WebClientRequestException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public List<PullRequestResponse> fetchPullRequestInfo(String owner, String repoName) {
        try {
            List<PullRequestResponse> allPullRequests = gitHubApiClient.getList(
                    "/repos/{owner}/{repo}/pulls?state=all&per_page=100",
                    PullRequestResponse.class, owner, repoName
            );

            LocalDateTime sixMonthsAgo = getSixMonthsAgo();
            return allPullRequests.stream()
                    .filter(pr -> parseGitHubDate(pr.created_at()).isAfter(sixMonthsAgo))
                    .collect(Collectors.toList());

        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.GITHUB_REPO_NOT_FOUND) {
                return Collections.emptyList();
            }
            throw e;
        }
    }

    @Retryable(
            retryFor = {WebClientResponseException.ServiceUnavailable.class,
                    WebClientResponseException.InternalServerError.class,
                    WebClientRequestException.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public Map<String, Integer> fetchLanguages(String owner, String repoName) {
        try {
            return gitHubApiClient.get("/repos/{owner}/{repo}/languages", Map.class, owner, repoName);
        } catch (BusinessException e) {
            if (e.getErrorCode() == ErrorCode.GITHUB_REPO_NOT_FOUND) {
                return Collections.emptyMap();
            }
            throw e;
        }
    }

    private LocalDateTime getSixMonthsAgo() {
        return LocalDateTime.now().minusMonths(COMMUNITY_ANALYSIS_MONTHS);
    }

    private LocalDateTime parseGitHubDate(String dateString) {
        try {
            return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            return LocalDateTime.MIN;
        }
    }
}
