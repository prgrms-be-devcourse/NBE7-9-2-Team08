package com.backend.domain.repository.service.fetcher;

import com.backend.domain.repository.dto.response.github.*;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import com.backend.global.github.GitHubApiClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Recover;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;
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
            noRetryFor = {WebClientResponseException.NotFound.class,  // 404, 401 에러는 재시도 X
                    WebClientResponseException.Unauthorized.class},
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
            noRetryFor = {WebClientResponseException.NotFound.class,
                    WebClientResponseException.Unauthorized.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public String fetchReadmeContent(String owner, String repoName) {
        return gitHubApiClient.getRaw("/repos/{owner}/{repo}/readme", owner, repoName);
    }

    @Retryable(
            retryFor = {WebClientResponseException.ServiceUnavailable.class,
                    WebClientResponseException.InternalServerError.class,
                    WebClientRequestException.class},
            noRetryFor = {WebClientResponseException.NotFound.class,
                    WebClientResponseException.Unauthorized.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public List<CommitResponse> fetchCommitInfo(String owner, String repoName, String since) {
        return gitHubApiClient.getList(
                "/repos/{owner}/{repo}/commits?since={since}&per_page=100", CommitResponse.class, owner, repoName, since
        );
    }

    @Retryable(
            retryFor = {WebClientResponseException.ServiceUnavailable.class,
                    WebClientResponseException.InternalServerError.class,
                    WebClientRequestException.class},
            noRetryFor = {WebClientResponseException.NotFound.class,
                    WebClientResponseException.Unauthorized.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public TreeResponse fetchRepositoryTreeInfo(String owner, String repoName, String defaultBranch) {
        return gitHubApiClient.get(
                "/repos/{owner}/{repo}/git/trees/{sha}?recursive=1", TreeResponse.class, owner, repoName, defaultBranch
        );
    }

    @Retryable(
            retryFor = {WebClientResponseException.ServiceUnavailable.class,
                    WebClientResponseException.InternalServerError.class,
                    WebClientRequestException.class},
            noRetryFor = {WebClientResponseException.NotFound.class,
                    WebClientResponseException.Unauthorized.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public List<IssueResponse> fetchIssueInfo(String owner, String repoName) {
        List<IssueResponse> allIssues = gitHubApiClient.getList(
                "/repos/{owner}/{repo}/issues?state=all&per_page=100", IssueResponse.class, owner, repoName);

        LocalDateTime sixMonthsAgo = getSixMonthsAgo();
        return allIssues.stream()
                .filter(IssueResponse::isPureIssue)
                .filter(issue -> parseGitHubDate(issue.created_at()).isAfter(sixMonthsAgo))
                .collect(Collectors.toList());
    }

    @Retryable(
            retryFor = {WebClientResponseException.ServiceUnavailable.class,
                    WebClientResponseException.InternalServerError.class,
                    WebClientRequestException.class},
            noRetryFor = {WebClientResponseException.NotFound.class,
                    WebClientResponseException.Unauthorized.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public List<PullRequestResponse> fetchPullRequestInfo(String owner, String repoName) {
        List<PullRequestResponse> allPullRequests = gitHubApiClient.getList(
                "/repos/{owner}/{repo}/pulls?state=all&per_page=100", PullRequestResponse.class, owner, repoName);

        LocalDateTime sixMonthsAgo = getSixMonthsAgo();
        return allPullRequests.stream()
                .filter(pr -> parseGitHubDate(pr.created_at()).isAfter(sixMonthsAgo))
                .collect(Collectors.toList());
    }

    @Retryable(
            retryFor = {WebClientResponseException.ServiceUnavailable.class,
                    WebClientResponseException.InternalServerError.class,
                    WebClientRequestException.class},
            noRetryFor = {WebClientResponseException.NotFound.class,
                    WebClientResponseException.Unauthorized.class},
            maxAttempts = 2,
            backoff = @Backoff(delay = 1000)
    )
    public Map<String, Integer> fetchLanguages(String owner, String repoName) {
        return gitHubApiClient.get("/repos/{owner}/{repo}/languages", Map.class, owner, repoName);
    }

    private LocalDateTime getSixMonthsAgo() {
        return LocalDateTime.now().minusMonths(COMMUNITY_ANALYSIS_MONTHS);
    }

    private LocalDateTime parseGitHubDate(String dateString) {
        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

    @Recover  // 재시도 실패 시 호출되는 메서드
    public RepoResponse recover(WebClientResponseException e, String owner, String repoName) {
        log.error("GitHub API 재시도 실패: {}/{}", owner, repoName, e);
        throw new BusinessException(ErrorCode.GITHUB_API_FAILED);
    }
}
