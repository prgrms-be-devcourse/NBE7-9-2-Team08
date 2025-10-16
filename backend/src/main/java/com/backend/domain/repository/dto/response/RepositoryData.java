package com.backend.domain.repository.dto.response;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

// === Analysis로 넘길 핵심 DTO ===
@Data
public class RepositoryData {
    // ===== 메타정보 (기본) =====
    private String repositoryName;
    private String repositoryUrl;
    private String description;
    private String primaryLanguage;
    private LocalDateTime repositoryCreatedAt;

    // ===== 1. 유지보수성 (Maintainability) =====
    private LocalDateTime lastCommitDate;
    private int daysSinceLastCommit;
    private int commitCountLast90Days;
    private List<CommitInfo> recentCommits;

    // ===== 2. 문서화 품질 (Documentation) =====
    private boolean hasReadme;
    private int readmeLength;
    private int readmeSectionCount;
    private List<String> readmeSectionTitles;
    private String readmeContent;

    // ===== 3. 보안 (Security) =====
    private boolean hasSensitiveFile;
    private List<String> sensitiveFilePaths;
    private boolean hasBuildFile;
    private List<String> buildFiles;

    // ===== 4. 테스트 구성 (Testing) =====
    private boolean hasTestDirectory;
    private int testFileCount;
    private int sourceFileCount;
    private double testCoverageRatio;

    // ===== 5. CI/CD (Build & Deployment) =====
    private boolean hasCICD;
    private List<String> cicdFiles;
    private boolean hasDockerfile;

    // ===== 6. 커뮤니티 활성도 (Community) =====
    private int issueCountLast6Months;
    private int closedIssueCountLast6Months;
    private int pullRequestCountLast6Months;
    private int mergedPullRequestCountLast6Months;
    private List<IssueInfo> recentIssues;
    private List<PullRequestInfo> recentPullRequests;

    // ===== 내부 클래스 (상세 정보) =====
    @Data
    public static class CommitInfo {
        private String message;
        private String author;
        private LocalDateTime committedDate;
    }

    @Data
    public static class IssueInfo {
        private String title;
        private String state;
        private LocalDateTime createdAt;
        private LocalDateTime closedAt;
    }

    @Data
    public static class PullRequestInfo {
        private String title;
        private String state;
        private LocalDateTime createdAt;
        private LocalDateTime mergedAt;
    }
}