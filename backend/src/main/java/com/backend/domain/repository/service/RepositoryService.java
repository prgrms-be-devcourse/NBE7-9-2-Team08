package com.backend.domain.repository.service;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.analysis.service.SseProgressNotifier;
import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.*;
import com.backend.domain.repository.entity.Language;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.entity.RepositoryLanguage;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.domain.repository.repository.RepositoryLanguageRepository;
import com.backend.domain.repository.service.fetcher.GitHubDataFetcher;
import com.backend.domain.repository.service.mapper.*;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final UserRepository userRepository;
    private final GitHubDataFetcher gitHubDataFetcher;
    private final RepositoriesMapper repositoriesMapper;
    private final RepositoryInfoMapper repositoryInfoMapper;
    private final CommitInfoMapper commitInfoMapper;
    private final ReadmeInfoMapper readmeInfoMapper;
    private final SecurityInfoMapper securityInfoMapper;
    private final TestInfoMapper testInfoMapper;
    private final CicdInfoMapper cicdInfoMapper;
    private final IssueInfoMapper issueInfoMapper;
    private final PullRequestInfoMapper pullRequestInfoMapper;
    private final RepositoryJpaRepository repositoryJpaRepository;
    private final SseProgressNotifier sseProgressNotifier;

    @Transactional
    public RepositoryData fetchAndSaveRepository(String owner, String repo, Long userId) {
        try {
            return fetchCompleteRepositoryData(owner, repo, userId);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            log.error("❌ Repository 수집 중 오류 발생", e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    // Github 저장소 데이터를 수집하고 DB에 저장 및 RepositoryData로 매핑
    @Transactional
    public RepositoryData fetchCompleteRepositoryData(String owner, String repo, Long userId) {
        RepositoryData data = new RepositoryData();

        try {
            // 1. 기본 정보 수집 및 매핑 + Repositories 테이블 저장
            sseProgressNotifier.notify(userId, "status", "GitHub 연결 중");
            RepoResponse repoInfo = gitHubDataFetcher.fetchRepositoryInfo(owner, repo);

            validateRepositorySize(repoInfo.size());

            repositoryInfoMapper.mapBasicInfo(data, repoInfo);

            // 2. 커밋 데이터 수집 및 매핑
            sseProgressNotifier.notify(userId, "status", "커밋 히스토리 분석");
            ZonedDateTime ninetyDaysAgoUtc = ZonedDateTime.now(ZoneOffset.UTC).minus(90, ChronoUnit.DAYS);
            String sinceParam = ninetyDaysAgoUtc.format(DateTimeFormatter.ISO_INSTANT);
            List<CommitResponse> commits = gitHubDataFetcher.fetchCommitInfo(owner, repo, sinceParam);
            commitInfoMapper.mapCommitInfo(data, commits);

            // 3. README 데이터 수집 및 매핑
            sseProgressNotifier.notify(userId, "status", "문서화 품질 분석");
            String readme = gitHubDataFetcher.fetchReadmeContent(owner, repo).orElse("");
            readmeInfoMapper.mapReadmeInfo(data, readme);

            // 4. 보안 관리 데이터 수집 및 매핑
            sseProgressNotifier.notify(userId, "status", "보안 구성 분석");
            TreeResponse tree = gitHubDataFetcher.fetchRepositoryTreeInfo(owner, repo, repoInfo.defaultBranch()).orElse(null);
            securityInfoMapper.mapSecurityInfo(data, tree);

            // 5. 테스트 데이터 수집 및 매핑
            sseProgressNotifier.notify(userId, "status", "테스트 구성 분석");
            testInfoMapper.mapTestInfo(data, tree);

            // 6. CI/CD 데이터 수집 및 매핑
            sseProgressNotifier.notify(userId, "status", "CI/CD 설정 분석");
            cicdInfoMapper.mapCicdInfo(data, tree);

            // 7. 커뮤니티 활성도 데이터 수집 및 매핑
            sseProgressNotifier.notify(userId, "status", "커뮤니티 활동 분석");
            List<IssueResponse> issues = gitHubDataFetcher.fetchIssueInfo(owner, repo);
            issueInfoMapper.mapIssueInfo(data, issues);
            List<PullRequestResponse> prs = gitHubDataFetcher.fetchPullRequestInfo(owner, repo);
            pullRequestInfoMapper.mapPullRequestInfo(data, prs);

            // Entity 저장 로직
            saveOrUpdateRepository(repoInfo, owner, repo, userId);

            return data;
        } catch (BusinessException e) {
            sseProgressNotifier.notify(userId, "error", "❌ " + e.getErrorCode().getMessage());
            throw e;

        } catch (Exception e) {
            sseProgressNotifier.notify(userId, "error", "❌ Repository 데이터 수집 실패: " + e.getMessage());
            throw e;
        }
    }

    /* Repository Entity를 DB에 저장하거나 기존 데이터 업데이트
    * 같은 htmlUrl + userId 조합이 존재하면 업데이트, 없으면 신규 데이터 저장 */
    private Repositories saveOrUpdateRepository(RepoResponse repoInfo, String owner, String repo, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        Map<String, Integer> languagesData = gitHubDataFetcher.fetchLanguages(owner, repo);

        return repositoryJpaRepository.findByHtmlUrlAndUserId(repoInfo.htmlUrl(), userId)
                .map(existing -> {
                    existing.updateFrom(repoInfo);
                    existing.updateLanguagesFrom(languagesData);
                    return existing;
                })
                .orElseGet(() -> {
                    Repositories newRepo = repositoriesMapper.toEntity(repoInfo, user);
                    newRepo.updateLanguagesFrom(languagesData);
                    return repositoryJpaRepository.save(newRepo);
                });
    }

    // 특정 사용자의 모든 Repository 조회
    public List<Repositories> findRepositoryByUser(Long userId) {
        return repositoryJpaRepository.findByUserId(userId);
    }

    // Repository ID로 단건 조회
    public Optional<Repositories> findById(Long repositoriesId) {
        return repositoryJpaRepository.findById(repositoriesId);
    }

    // 저장소 크기 검증
    private void validateRepositorySize(Integer sizeInKb) {
        if (sizeInKb == null) {
            return;
        }

        final int MAX_SIZE_KB = 1_000_000;

        if (sizeInKb > MAX_SIZE_KB) {
            log.warn("저장소 크기 초과: {}KB (제한: {}KB)", sizeInKb, MAX_SIZE_KB);
            throw new BusinessException(ErrorCode.GITHUB_REPO_TOO_LARGE);
        }
    }
}
