package com.backend.domain.repository.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.CommitResponse;
import com.backend.domain.repository.service.mapper.CommitInfoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.when;

class CommitInfoMapperTest {

    private final CommitInfoMapper mapper = new CommitInfoMapper();

    @Test
    @DisplayName("커밋이 하나도 없는 저장소 - 기본값 세팅 확인")
    void mapEmptyCommits_shouldSetDefaults() {
        RepositoryData data = new RepositoryData();
        mapper.mapCommitInfo(data, List.of());
        assertThat(data.getLastCommitDate()).isNull();
        assertThat(data.getCommitCountLast90Days()).isZero();
        assertThat(data.getRecentCommits()).isEmpty();
    }

    @Test
    @DisplayName("Commit 내부 필드가 null이어도 NPE 없이 동작해야 함")
    void mapCommitWithNullInnerFields_shouldBeSafe() {
        RepositoryData data = new RepositoryData();
        CommitResponse commitResponse = new CommitResponse(
                new CommitResponse.CommitDetails("initial commit", null)
        );
        mapper.mapCommitInfo(data, List.of(commitResponse));
        assertThat(data.getCommitCountLast90Days()).isEqualTo(1);
        assertThat(data.getRecentCommits()).hasSize(1);
    }

    @Test
    @DisplayName("CommitResponse의 commit() 자체가 null일 때")
    void mapCommitWithNullCommitField_shouldBeSafe() {
        RepositoryData data = new RepositoryData();
        CommitResponse nullCommit = org.mockito.Mockito.mock(CommitResponse.class);
        when(nullCommit.commit()).thenReturn(null);
        assertThatCode(() -> mapper.mapCommitInfo(data, List.of(nullCommit)))
                .doesNotThrowAnyException();
    }

    @Test
    @DisplayName("100자 이상의 커밋 메시지는 잘려야 함")
    void mapLongCommitMessage_shouldBeTruncated() {
        RepositoryData data = new RepositoryData();
        String longMessage = "a".repeat(150);
        CommitResponse commit = new CommitResponse(
                new CommitResponse.CommitDetails(
                        longMessage,
                        new CommitResponse.AuthorDetails("2025-01-01T00:00:00Z")
                )
        );
        mapper.mapCommitInfo(data, List.of(commit));
        assertThat(data.getRecentCommits().get(0).getMessage())
                .hasSize(100)
                .endsWith("...");
    }

    @Test
    @DisplayName("여러 줄 커밋 메시지는 첫 줄만 사용해야 함")
    void mapMultiLineCommitMessage_shouldUseFirstLineOnly() {
        RepositoryData data = new RepositoryData();
        String multiLineMessage = """
            feat: add new feature
            
            This is detailed description
            """;
        CommitResponse commit = new CommitResponse(
                new CommitResponse.CommitDetails(
                        multiLineMessage,
                        new CommitResponse.AuthorDetails("2025-01-01T00:00:00Z")
                )
        );
        mapper.mapCommitInfo(data, List.of(commit));
        assertThat(data.getRecentCommits().get(0).getMessage())
                .isEqualTo("feat: add new feature");
    }

    @Test
    @DisplayName("CommitResponse 자체가 null이 리스트에 포함되어도 안전해야 함")
    void mapCommitWithNullElement_shouldBeFiltered() {
        RepositoryData data = new RepositoryData();
        CommitResponse validCommit = new CommitResponse(
                new CommitResponse.CommitDetails(
                        "valid commit",
                        new CommitResponse.AuthorDetails("2025-01-01T00:00:00Z")
                )
        );
        List<CommitResponse> commits = new ArrayList<>();
        commits.add(validCommit);
        commits.add(null);
        assertThatCode(() -> mapper.mapCommitInfo(data, commits))
                .doesNotThrowAnyException();
    }
}
