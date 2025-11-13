package com.backend.domain.repository.dto;

import com.backend.domain.repository.dto.response.RepositoryData;

import java.time.LocalDateTime;
import java.util.List;

/**
 * RepositoryData 테스트용 Fixture
 * EvaluationServiceTest, AnalysisServiceTest 등에서 공통으로 사용
 */
public class RepositoryDataFixture {

    /**
     * 최소한의 필수 필드만 채운 RepositoryData
     */
    public static RepositoryData createMinimal() {
        RepositoryData data = new RepositoryData();

        // 필수 메타정보
        data.setRepositoryName("test-repo");
        data.setRepositoryUrl("https://github.com/owner/test-repo");
        data.setDescription("Test repository");
        data.setPrimaryLanguage("Java");
        data.setRepositoryCreatedAt(LocalDateTime.now().minusMonths(6));

        // 빈 리스트로 초기화 (null 방지)
        data.setRecentCommits(List.of());
        data.setReadmeSectionTitles(List.of());
        data.setReadmeContent("");
        data.setSensitiveFilePaths(List.of());
        data.setBuildFiles(List.of());
        data.setCicdFiles(List.of());
        data.setRecentIssues(List.of());
        data.setRecentPullRequests(List.of());

        return data;
    }

    /**
     * 모든 필드가 채워진 완전한 RepositoryData
     */
    public static RepositoryData createComplete() {
        RepositoryData data = new RepositoryData();

        // ===== 메타정보 =====
        data.setRepositoryName("complete-test-repo");
        data.setRepositoryUrl("https://github.com/owner/complete-test-repo");
        data.setDescription("Complete test repository for evaluation");
        data.setPrimaryLanguage("Java");
        data.setRepositoryCreatedAt(LocalDateTime.now().minusMonths(6));

        // ===== 1. 유지보수성 =====
        data.setLastCommitDate(LocalDateTime.now().minusDays(1));
        data.setDaysSinceLastCommit(1);
        data.setCommitCountLast90Days(50);
        data.setRecentCommits(List.of(
                createCommitInfo("feat: Add new feature", "developer", LocalDateTime.now().minusDays(1)),
                createCommitInfo("fix: Bug fix", "developer", LocalDateTime.now().minusDays(2))
        ));

        // ===== 2. 문서화 품질 =====
        data.setHasReadme(true);
        data.setReadmeLength(500);
        data.setReadmeSectionCount(3);
        data.setReadmeSectionTitles(List.of("Introduction", "Installation", "Usage"));
        data.setReadmeContent("# Test Repository\n\n## Introduction\nThis is a test.\n\n## Installation\nRun npm install.\n\n## Usage\nRun npm start.");

        // ===== 3. 보안 =====
        data.setHasSensitiveFile(false);
        data.setSensitiveFilePaths(List.of());
        data.setHasBuildFile(true);
        data.setBuildFiles(List.of("pom.xml"));

        // ===== 4. 테스트 구성 =====
        data.setHasTestDirectory(true);
        data.setTestFileCount(10);
        data.setSourceFileCount(20);
        data.setTestCoverageRatio(0.5);

        // ===== 5. CI/CD =====
        data.setHasCICD(true);
        data.setCicdFiles(List.of(".github/workflows/ci.yml"));
        data.setHasDockerfile(true);

        // ===== 6. 커뮤니티 활성도 =====
        data.setIssueCountLast6Months(5);
        data.setClosedIssueCountLast6Months(3);
        data.setPullRequestCountLast6Months(10);
        data.setMergedPullRequestCountLast6Months(8);
        data.setRecentIssues(List.of(
                createIssueInfo("Bug fix needed", "open", LocalDateTime.now().minusDays(2), null)
        ));
        data.setRecentPullRequests(List.of(
                createPullRequestInfo("Add feature X", "merged", LocalDateTime.now().minusDays(3), LocalDateTime.now().minusDays(1))
        ));

        return data;
    }

    // ===== 헬퍼 메서드들 =====

    public static RepositoryData.CommitInfo createCommitInfo(String message, String author, LocalDateTime date) {
        RepositoryData.CommitInfo commit = new RepositoryData.CommitInfo();
        commit.setMessage(message);
        commit.setCommittedDate(date);
        return commit;
    }

    public static RepositoryData.IssueInfo createIssueInfo(String title, String state, LocalDateTime createdAt, LocalDateTime closedAt) {
        RepositoryData.IssueInfo issue = new RepositoryData.IssueInfo();
        issue.setTitle(title);
        issue.setState(state);
        issue.setCreatedAt(createdAt);
        issue.setClosedAt(closedAt);
        return issue;
    }

    public static RepositoryData.PullRequestInfo createPullRequestInfo(String title, String state, LocalDateTime createdAt, LocalDateTime mergedAt) {
        RepositoryData.PullRequestInfo pr = new RepositoryData.PullRequestInfo();
        pr.setTitle(title);
        pr.setState(state);
        pr.setCreatedAt(createdAt);
        pr.setMergedAt(mergedAt);
        return pr;
    }
}