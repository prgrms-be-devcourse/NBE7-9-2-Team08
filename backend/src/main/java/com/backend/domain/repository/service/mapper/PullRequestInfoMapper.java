package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.PullRequestResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class PullRequestInfoMapper {

    public void mapPullRequestInfo(RepositoryData data, List<PullRequestResponse> response) {
        if (response == null || response.isEmpty()) {
            setEmptyPullRequest(data);
            return;
        }

        // 최근 6개월 PR 수
        data.setPullRequestCountLast6Months(response.size());

        // 6개월 내 머지된 PR 수
        long mergedPRCount = response.stream()
                .filter(pr -> pr.merged_at() != null)
                .count();
        data.setMergedPullRequestCountLast6Months((int) mergedPRCount);

        // 최근 5-10개 PR
        List<RepositoryData.PullRequestInfo> recentPRs = response.stream()
                .limit(10)
                .map(this::convertToPullRequestInfo)
                .collect(Collectors.toList());
        data.setRecentPullRequests(recentPRs);
    }

    private void setEmptyPullRequest(RepositoryData data) {
        data.setIssueCountLast6Months(0);
        data.setClosedIssueCountLast6Months(0);
        data.setRecentIssues(Collections.emptyList());
    }

    private RepositoryData.PullRequestInfo convertToPullRequestInfo(PullRequestResponse pr) {
        RepositoryData.PullRequestInfo pullRequestInfo = new RepositoryData.PullRequestInfo();
        pullRequestInfo.setTitle(pr.title());
        pullRequestInfo.setState(pr.state());
        pullRequestInfo.setCreatedAt(parseGitHubDate(pr.created_at()));
        pullRequestInfo.setMergedAt(parseGitHubDate(pr.merged_at()));

        return pullRequestInfo;
    }

    private LocalDateTime parseGitHubDate(String dateString) {
        if (dateString == null || dateString.trim().isEmpty()) {
            return null;
        }
        try {
            return LocalDateTime.parse(dateString, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (Exception e) {
            return null;
        }
    }
}
