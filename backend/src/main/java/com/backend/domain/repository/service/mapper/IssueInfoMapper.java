package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.IssueResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class IssueInfoMapper {

    public void mapIssueInfo(RepositoryData data, List<IssueResponse> response) {
        if (response == null || response.isEmpty()) {
            setEmptyIssue(data);
            return;
        }

        List<IssueResponse> validIssues = response.stream()
                .filter(issue -> issue != null)
                .collect(Collectors.toList());

        // 최근 6개월 이슈 수
        data.setIssueCountLast6Months(validIssues.size());

        // 6개월 내 해결된 이슈 수
        long closedIssueCount = validIssues.stream()
                .filter(IssueResponse::isClosed)
                .count();
        data.setClosedIssueCountLast6Months((int) closedIssueCount);

        // 최근 5-10개 이슈
        List<RepositoryData.IssueInfo> recentIssues = validIssues.stream()
                .limit(10)
                .map(this::convertToIssueInfo)
                .collect(Collectors.toList());
        data.setRecentIssues(recentIssues);
    }

    private void setEmptyIssue(RepositoryData data) {
        data.setIssueCountLast6Months(0);
        data.setClosedIssueCountLast6Months(0);
        data.setRecentIssues(Collections.emptyList());
    }

    private RepositoryData.IssueInfo convertToIssueInfo(IssueResponse issue) {
        RepositoryData.IssueInfo issueInfo = new RepositoryData.IssueInfo();
        issueInfo.setTitle(issue.title());
        issueInfo.setState(issue.state());
        issueInfo.setCreatedAt(parseGitHubDate(issue.created_at()));
        issueInfo.setClosedAt(parseGitHubDate(issue.closed_at()));
        return issueInfo;
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
