package com.backend.domain.repository.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.PullRequestResponse;
import com.backend.domain.repository.service.mapper.PullRequestInfoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PullRequestInfoMapperTest {

    private final PullRequestInfoMapper mapper = new PullRequestInfoMapper();

    @Test
    @DisplayName("created_at이 null이더라도 merged_at이 있으면 정상 처리")
    void prWithNullCreatedAt_shouldBeHandled() {
        RepositoryData data = new RepositoryData();
        PullRequestResponse pr = new PullRequestResponse(
                1L, "PR test", "closed", null, "2025-01-01T00:00:00Z"
        );
        mapper.mapPullRequestInfo(data, List.of(pr));
        assertThat(data.getRecentPullRequests()).hasSize(1);
        assertThat(data.getRecentPullRequests().get(0).getMergedAt()).isNotNull();
    }
}
