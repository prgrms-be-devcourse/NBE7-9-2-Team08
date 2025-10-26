package com.backend.domain.repository.mapper;


import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.CommitResponse;
import com.backend.domain.repository.dto.response.github.IssueResponse;
import com.backend.domain.repository.dto.response.github.PullRequestResponse;
import com.backend.domain.repository.dto.response.github.TreeResponse;
import com.backend.domain.repository.service.mapper.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class RepositoryMapperTest {

    private final ReadmeInfoMapper readmeInfoMapper = new ReadmeInfoMapper();
    private final CommitInfoMapper commitInfoMapper = new CommitInfoMapper();
    private final IssueInfoMapper issueInfoMapper = new IssueInfoMapper();
    private final PullRequestInfoMapper pullRequestInfoMapper = new PullRequestInfoMapper();
    private final CicdInfoMapper cicdInfoMapper = new CicdInfoMapper();
    private final SecurityInfoMapper securityInfoMapper = new SecurityInfoMapper();

    @Test
    @DisplayName("README가 비어 있을 때 - 기본값 설정 확인")
    void mapEmptyReadme_shouldSetDefaultValues() {
        RepositoryData data = new RepositoryData();

        // 빈 README
        readmeInfoMapper.mapReadmeInfo(data, "");

        assertThat(data.isHasReadme()).isFalse();
        assertThat(data.getReadmeContent()).isEmpty();
        assertThat(data.getReadmeSectionCount()).isZero();
        assertThat(data.getReadmeSectionTitles()).isEmpty();
    }

    @Test
    @DisplayName("커밋이 하나도 없는 저장소 - 기본값 세팅 확인")
    void mapEmptyCommits_shouldSetDefaults() {
        RepositoryData data = new RepositoryData();

        // 빈 커밋 리스트
        commitInfoMapper.mapCommitInfo(data, Collections.emptyList());

        assertThat(data.getLastCommitDate()).isNull();
        assertThat(data.getDaysSinceLastCommit()).isZero();
        assertThat(data.getCommitCountLast90Days()).isZero();
        assertThat(data.getRecentCommits()).isEmpty();
    }

    @Test
    @DisplayName("Issue와 Pull Request가 모두 없는 경우 - 기본값 정상 세팅")
    void mapEmptyIssueAndPullRequest_shouldSetDefaults() {
        RepositoryData data = new RepositoryData();

        issueInfoMapper.mapIssueInfo(data, Collections.emptyList());
        pullRequestInfoMapper.mapPullRequestInfo(data, Collections.emptyList());

        assertThat(data.getIssueCountLast6Months()).isZero();
        assertThat(data.getClosedIssueCountLast6Months()).isZero();
        assertThat(data.getRecentIssues()).isEmpty();

        assertThat(data.getPullRequestCountLast6Months()).isZero();
        assertThat(data.getMergedPullRequestCountLast6Months()).isZero();
        assertThat(data.getRecentPullRequests()).isEmpty();
    }

    @Test
    @DisplayName("TreeResponse가 null이거나 비어 있을 때 - CI/CD와 Security Mapper가 NPE 없이 작동해야 함")
    void mapNullOrEmptyTree_shouldHandleGracefully() {
        RepositoryData cicdData = new RepositoryData();
        RepositoryData secData = new RepositoryData();

        // case 1. null
        cicdInfoMapper.mapCicdInfo(cicdData, null);
        securityInfoMapper.mapSecurityInfo(secData, null);

        assertThat(cicdData.isHasCICD()).isFalse();
        assertThat(cicdData.getCicdFiles()).isEmpty();
        assertThat(secData.isHasSensitiveFile()).isFalse();
        assertThat(secData.getBuildFiles()).isEmpty();

        // case 2. empty TreeResponse
        TreeResponse emptyTree = new TreeResponse(Collections.emptyList(), false);

        RepositoryData cicdData2 = new RepositoryData();
        RepositoryData secData2 = new RepositoryData();

        cicdInfoMapper.mapCicdInfo(cicdData2, emptyTree);
        securityInfoMapper.mapSecurityInfo(secData2, emptyTree);

        assertThat(cicdData2.isHasCICD()).isFalse();
        assertThat(cicdData2.getCicdFiles()).isEmpty();
        assertThat(secData2.isHasBuildFile()).isFalse();
        assertThat(secData2.isHasSensitiveFile()).isFalse();
    }

    @Test
    @DisplayName("TreeResponse는 있지만 tree() 필드가 null인 경우")
    void mapTreeWithNullTreeField_shouldHandleGracefully() {
        RepositoryData data = new RepositoryData();

        TreeResponse treeWithNullList = mock(TreeResponse.class);
        when(treeWithNullList.tree()).thenReturn(null);

        // NPE 발생하지 않아야 함
        assertThatCode(() -> {
            cicdInfoMapper.mapCicdInfo(data, treeWithNullList);
            securityInfoMapper.mapSecurityInfo(data, treeWithNullList);
        }).doesNotThrowAnyException();

        assertThat(data.isHasCICD()).isFalse();
        assertThat(data.isHasSensitiveFile()).isFalse();
    }

    @Test
    @DisplayName("README 내 코드 블록 안의 #은 섹션으로 인식하지 않아야 함")
    void mapReadmeWithCodeBlock_shouldIgnoreHeadersInCode() {
        RepositoryData data = new RepositoryData();
        String readme = """
                # Main Title

                ```bash
                # not a header
                echo "hello"
                ```

                ## Section 2
                """;

        readmeInfoMapper.mapReadmeInfo(data, readme);

        assertThat(data.isHasReadme()).isTrue();
        assertThat(data.getReadmeSectionTitles())
                .containsExactly("Main Title", "Section 2");
        assertThat(data.getReadmeSectionCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Commit 내부 필드가 null이어도 NPE 없이 동작해야 함")
    void mapCommitWithNullInnerFields_shouldBeSafe() {
        RepositoryData data = new RepositoryData();

        CommitResponse commitResponse = new CommitResponse(
                new CommitResponse.CommitDetails("initial commit", null)
        );

        commitInfoMapper.mapCommitInfo(data, List.of(commitResponse));

        assertThat(data.getLastCommitDate()).isNotNull(); // parseCommitDate()가 now()로 대체됨
        assertThat(data.getCommitCountLast90Days()).isEqualTo(1);
        assertThat(data.getRecentCommits()).hasSize(1);
    }

    @Test
    @DisplayName("CommitResponse의 commit() 자체가 null일 때")
    void mapCommitWithNullCommitField_shouldBeSafe() {
        RepositoryData data = new RepositoryData();

        CommitResponse nullCommit = mock(CommitResponse.class);
        when(nullCommit.commit()).thenReturn(null);

        assertThatCode(() -> {
            commitInfoMapper.mapCommitInfo(data, List.of(nullCommit));
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("Issue/PR 날짜 필드가 null이거나 형식이 이상해도 예외 없이 null 반환해야 함")
    void mapIssuePrWithInvalidDate_shouldHandleGracefully() {
        RepositoryData data = new RepositoryData();

        IssueResponse invalidIssue = new IssueResponse(
                1L, "broken issue", "open", "", "invalid-date", null
        );

        PullRequestResponse invalidPr = new PullRequestResponse(
                1L, "weird PR", "closed", null, "2025-15-99T00:00:00Z"
        );

        issueInfoMapper.mapIssueInfo(data, List.of(invalidIssue));
        pullRequestInfoMapper.mapPullRequestInfo(data, List.of(invalidPr));

        assertThat(data.getRecentIssues().get(0).getCreatedAt()).isNull();
        assertThat(data.getRecentPullRequests().get(0).getMergedAt()).isNull();
    }

    @Test
    @DisplayName("IssueResponse 자체가 null이 리스트에 포함되어도 안전해야 함")
    void mapIssueWithNullElement_shouldBeFiltered() {
        RepositoryData data = new RepositoryData();

        IssueResponse validIssue = new IssueResponse(
                1L, "Valid", "open", "2025-01-01T00:00:00Z", null, null
        );

        // null 포함된 리스트 (실제로는 발생하지 않겠지만 방어 코드 확인)
        List<IssueResponse> issues = new ArrayList<>();
        issues.add(validIssue);
        issues.add(null);

        assertThatCode(() -> {
            issueInfoMapper.mapIssueInfo(data, issues);
        }).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("README 내용이 공백만 있을 때 hasReadme=false 여야 함")
    void mapWhitespaceOnlyReadme_shouldBeHandled() {
        RepositoryData data = new RepositoryData();
        readmeInfoMapper.mapReadmeInfo(data, "   \n  \n");

        assertThat(data.isHasReadme()).isFalse();
        assertThat(data.getReadmeLength()).isZero();
    }

    @Test
    @DisplayName("TreeResponse가 truncated=true여도 정상 처리되어야 함")
    void mapTruncatedTree_shouldBeHandledGracefully() {
        RepositoryData data = new RepositoryData();

        TreeResponse truncatedTree = new TreeResponse(Collections.emptyList(), true);
        cicdInfoMapper.mapCicdInfo(data, truncatedTree);
        securityInfoMapper.mapSecurityInfo(data, truncatedTree);

        assertThat(data.isHasCICD()).isFalse();
        assertThat(data.isHasSensitiveFile()).isFalse();
    }

    @Test
    @DisplayName("secret.json은 탐지하지만 secret.json.example은 탐지하지 않아야 함")
    void securityMapper_shouldDistinguishSensitiveAndSafeFiles() {
        RepositoryData data = new RepositoryData();

        TreeResponse tree = new TreeResponse(
                List.of(
                        new TreeResponse.TreeItem("config/secret.json", "blob"),
                        new TreeResponse.TreeItem("config/secret.json.example", "blob")
                ),
                false
        );

        securityInfoMapper.mapSecurityInfo(data, tree);

        assertThat(data.isHasSensitiveFile()).isTrue();
        assertThat(data.getSensitiveFilePaths()).contains("config/secret.json");
        assertThat(data.getSensitiveFilePaths()).doesNotContain("config/secret.json.example");
    }

    @Test
    @DisplayName("IssueResponse에서 pull_request가 존재하면 순수 Issue로 인식하지 않아야 함")
    void issueWithPullRequestField_shouldBeExcludedByFilter() {
        IssueResponse issueWithPr = new IssueResponse(
                1L, "PR disguised as issue", "open", "2025-01-01T00:00:00Z", null,
                new IssueResponse.PullRequest("https://github.com/pr-url")
        );

        IssueResponse pureIssue = new IssueResponse(
                2L, "Normal issue", "open", "2025-01-01T00:00:00Z", null, null
        );

        List<IssueResponse> filtered = List.of(issueWithPr, pureIssue).stream()
                .filter(IssueResponse::isPureIssue)
                .toList();

        assertThat(filtered).hasSize(1);
        assertThat(filtered.get(0).title()).isEqualTo("Normal issue");
    }

    @Test
    @DisplayName("created_at이 null이더라도 merged_at이 있으면 NPE 없이 처리")
    void prWithNullCreatedAt_shouldBeHandled() {
        RepositoryData data = new RepositoryData();
        PullRequestResponse pr = new PullRequestResponse(
                1L, "PR test", "closed", null, "2025-01-01T00:00:00Z"
        );

        pullRequestInfoMapper.mapPullRequestInfo(data, List.of(pr));

        assertThat(data.getRecentPullRequests()).hasSize(1);
        assertThat(data.getRecentPullRequests().get(0).getMergedAt()).isNotNull();
        assertThat(data.getRecentPullRequests().get(0).getCreatedAt()).isNull();
    }

    @Test
    @DisplayName("TreeItem이 blob이 아닌 경우 무시되어야 함")
    void treeWithNonBlobItems_shouldBeIgnored() {
        RepositoryData cicd = new RepositoryData();
        RepositoryData sec = new RepositoryData();

        TreeResponse.TreeItem dirItem = new TreeResponse.TreeItem("src/", "tree");
        TreeResponse.TreeItem commitItem = new TreeResponse.TreeItem("submodule", "commit");
        TreeResponse tree = new TreeResponse(List.of(dirItem, commitItem), false);

        cicdInfoMapper.mapCicdInfo(cicd, tree);
        securityInfoMapper.mapSecurityInfo(sec, tree);

        assertThat(cicd.isHasCICD()).isFalse();
        assertThat(sec.isHasSensitiveFile()).isFalse();
    }

    @Test
    @DisplayName("DockerFile(대소문자 혼합)도 감지되어야 함")
    void dockerfileCaseInsensitive_shouldBeDetected() {
        RepositoryData data = new RepositoryData();
        TreeResponse tree = new TreeResponse(
                List.of(new TreeResponse.TreeItem("infra/DockerFile", "blob")),
                false
        );

        cicdInfoMapper.mapCicdInfo(data, tree);
        assertThat(data.isHasDockerfile()).isTrue();
    }

    @Test
    @DisplayName("README가 HTML 헤더를 포함하더라도 Markdown 헤더만 인식해야 함")
    void readmeWithHtmlHeaders_shouldIgnoreThem() {
        RepositoryData data = new RepositoryData();
        String htmlReadme = """
                <h1>Main Title</h1>
                # Markdown Header
                <h2>Sub Title</h2>
                ## Markdown Sub
                """;

        readmeInfoMapper.mapReadmeInfo(data, htmlReadme);

        assertThat(data.getReadmeSectionTitles())
                .containsExactly("Markdown Header", "Markdown Sub");
    }
}