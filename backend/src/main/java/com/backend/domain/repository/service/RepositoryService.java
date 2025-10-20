package com.backend.domain.repository.service;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.*;
import com.backend.domain.repository.entity.Language;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.domain.repository.service.fetcher.GitHubDataFetcher;
import com.backend.domain.repository.service.mapper.*;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class RepositoryService {

    // 임시
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

    @Transactional
    public RepositoryData fetchAndSaveRepository(String owner, String repo) {
        try {
            return fetchCompleteRepositoryData(owner, repo);
        } catch (BusinessException e) {
            String errorCode = (e.getErrorCode() != null) ? e.getErrorCode().getCode() : "UNKNOWN";
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Transactional
    public RepositoryData fetchCompleteRepositoryData(String owner, String repo) {
        RepositoryData data = new RepositoryData();

        // 1. 기본 정보 수집 및 매핑 + Repositories 테이블 저장
        RepoResponse repoInfo = gitHubDataFetcher.fetchRepositoryInfo(owner, repo);
        repositoryInfoMapper.mapBasicInfo(data, repoInfo);

        // 2. 커밋 데이터 수집 및 매핑
        ZonedDateTime ninetyDaysAgoUtc = ZonedDateTime.now(ZoneOffset.UTC).minus(90, ChronoUnit.DAYS);
        String sinceParam = ninetyDaysAgoUtc.format(DateTimeFormatter.ISO_INSTANT);
        List<CommitResponse> commitInfo = gitHubDataFetcher.fetchCommitInfo(owner, repo, sinceParam);
        commitInfoMapper.mapCommitInfo(data, commitInfo);

        // 3. README 데이터 수집 및 매핑
        String readmeInfo = gitHubDataFetcher.fetchReadmeContent(owner, repo);
        readmeInfoMapper.mapReadmeInfo(data, readmeInfo);

        // 4. 보안 관리 데이터 수집 및 매핑
        TreeResponse securityInfo = gitHubDataFetcher.fetchRepositoryTreeInfo(owner, repo, repoInfo.defaultBranch());
        securityInfoMapper.mapSecurityInfo(data, securityInfo);

        // 5. 테스트 데이터 수집 및 매핑
        TreeResponse testInfo = gitHubDataFetcher.fetchRepositoryTreeInfo(owner, repo, repoInfo.defaultBranch());
        testInfoMapper.mapTestInfo(data, testInfo);

        // 6. CI/CD 데이터 수집 및 매핑
        TreeResponse cicdInfo = gitHubDataFetcher.fetchRepositoryTreeInfo(owner, repo, repoInfo.defaultBranch());
        cicdInfoMapper.mapCicdInfo(data, cicdInfo);

        // 7. 커뮤니티 활성도 데이터 수집 및 매핑
        List<IssueResponse> issueInfo = gitHubDataFetcher.fetchIssueInfo(owner, repo);
        issueInfoMapper.mapIssueInfo(data, issueInfo);

        List<PullRequestResponse> pullRequestInfo = gitHubDataFetcher.fetchPullRequestInfo(owner, repo);
        pullRequestInfoMapper.mapPullRequestInfo(data, pullRequestInfo);

        // Entity 저장 로직
        Repositories savedRepository = saveOrUpdateRepository(repoInfo, owner, repo);


        return data;
    }

    private Repositories saveOrUpdateRepository(RepoResponse repoInfo, String owner, String repo) {
        User defaultUser = userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR));

        Map<String, Integer> languagesData = gitHubDataFetcher.fetchLanguages(owner, repo);

        return repositoryJpaRepository.findByHtmlUrl(repoInfo.htmlUrl())
                .map(existing -> {
                    existing.updateFrom(repoInfo);
                    existing.updateLanguagesFrom(languagesData);
                    return existing;
                })
                .orElseGet(() -> {
                    Repositories newRepo = repositoriesMapper.toEntity(repoInfo, defaultUser);
                    newRepo.updateLanguagesFrom(languagesData);
                    return repositoryJpaRepository.save(newRepo);
                });
    }

    // Repository에서 member로 리포지토리 찾기
    public List<Repositories> findRepositoryByMember(Long userId){
        return repositoryJpaRepository.findByUserId(userId);
    }

    // Repository ID로 언어 조회
    public List<Language> findLanguagesByRepisotryId(Long gitRepositoryId){
        return repositoryJpaRepository.findLanguagesByRepositoryId(gitRepositoryId);
    }

    // repository 삭제
    @Transactional
    public void delete(Long repositoriesId){
        if (repositoriesId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Repositories targetRepository = repositoryJpaRepository.findById(repositoriesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND));

        repositoryJpaRepository.delete(targetRepository);
    }
}
