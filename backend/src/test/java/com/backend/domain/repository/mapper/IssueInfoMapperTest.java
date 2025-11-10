package com.backend.domain.repository.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.IssueResponse;
import com.backend.domain.repository.service.mapper.IssueInfoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

class IssueInfoMapperTest {

    private final IssueInfoMapper mapper = new IssueInfoMapper();

    @Test
    @DisplayName("IssueResponse가 null이 포함되어도 안전하게 처리해야 함")
    void mapIssueWithNullElement_shouldBeFiltered() {
        RepositoryData data = new RepositoryData();
        IssueResponse valid = new IssueResponse(
                1L, "Valid", "open", "2025-01-01T00:00:00Z", null, null
        );
        List<IssueResponse> list = new ArrayList<>();
        list.add(valid);
        list.add(null);
        assertThatCode(() -> mapper.mapIssueInfo(data, list))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("IssueResponse의 pull_request 필드가 존재하면 필터링되어야 함")
    void issueWithPullRequestField_shouldBeExcluded() {
        IssueResponse issueWithPr = new IssueResponse(
                1L, "PR disguised", "open", "2025-01-01T00:00:00Z", null,
                new IssueResponse.PullRequest("url")
        );
        IssueResponse pureIssue = new IssueResponse(
                2L, "Real issue", "open", "2025-01-01T00:00:00Z", null, null
        );
        List<IssueResponse> filtered = List.of(issueWithPr, pureIssue).stream()
                .filter(IssueResponse::isPureIssue)
                .toList();
        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).title()).isEqualTo("Real issue");
    }

    @Test
    @DisplayName("날짜 필드가 null 또는 형식 이상해도 NPE 없이 null 반환")
    void mapInvalidDate_shouldHandleGracefully() {
        RepositoryData data = new RepositoryData();
        IssueResponse issue = new IssueResponse(
                1L, "broken", "open", "invalid", null, null
        );
        mapper.mapIssueInfo(data, List.of(issue));
        assertThat(data.getRecentIssues().get(0).getCreatedAt()).isNull();
    }
}
