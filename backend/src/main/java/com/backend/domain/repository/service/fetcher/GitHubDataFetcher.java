package com.backend.domain.repository.service.fetcher;

import com.backend.domain.repository.dto.response.github.*;
import com.backend.global.github.GitHubApiClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class GitHubDataFetcher {
    private final GitHubApiClient gitHubApiClient;
    private static final int COMMUNITY_ANALYSIS_MONTHS = 6;

    public RepoResponse fetchRepositoryInfo(String owner, String repoName) {
        return gitHubApiClient.get("/repos/{owner}/{repo}", RepoResponse.class, owner, repoName);
    }

    public String fetchReadmeContent(String owner, String repoName) {
        return gitHubApiClient.getRaw("/repos/{owner}/{repo}/readme", owner, repoName);
    }

    public List<CommitResponse> fetchCommitInfo(String owner, String repoName, String since) {
        return gitHubApiClient.getList(
                "/repos/{owner}/{repo}/commits?since={since}&per_page=100", CommitResponse.class, owner, repoName, since
        );
    }

    public TreeResponse fetchRepositoryTreeInfo(String owner, String repoName, String defaultBranch) {
        return gitHubApiClient.get(
                "/repos/{owner}/{repo}/git/trees/{sha}?recursive=1", TreeResponse.class, owner, repoName, defaultBranch
        );
    }

    public List<IssueResponse> fetchIssueInfo(String owner, String repoName) {
        List<IssueResponse> allIssues = gitHubApiClient.getList(
                "/repos/{owner}/{repo}/issues?state=all&per_page=100", IssueResponse.class, owner, repoName);

        LocalDateTime sixMonthsAgo = getSixMonthsAgo();
        return allIssues.stream()
                .filter(IssueResponse::isPureIssue)
                .filter(issue -> parseGitHubDate(issue.created_at()).isAfter(sixMonthsAgo))
                .collect(Collectors.toList());
    }

    public List<PullRequestResponse> fetchPullRequestInfo(String owner, String repoName) {
        List<PullRequestResponse> allPullRequests = gitHubApiClient.getList(
                "/repos/{owner}/{repo}/pulls?state=all&per_page=100", PullRequestResponse.class, owner, repoName);

        LocalDateTime sixMonthsAgo = getSixMonthsAgo();
        return allPullRequests.stream()
                .filter(pr -> parseGitHubDate(pr.created_at()).isAfter(sixMonthsAgo))
                .collect(Collectors.toList());
    }

    private LocalDateTime getSixMonthsAgo() {
        return LocalDateTime.now().minusMonths(COMMUNITY_ANALYSIS_MONTHS);
    }

    private LocalDateTime parseGitHubDate(String dateString) {
        return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
    }

}
