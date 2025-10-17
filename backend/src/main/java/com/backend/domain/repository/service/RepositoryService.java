package com.backend.domain.repository.service;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.RepoResponse;
import com.backend.domain.repository.entity.Language;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import com.backend.domain.repository.service.fetcher.GitHubDataFetcher;
import com.backend.domain.repository.service.mapper.ReadmeInfoMapper;
import com.backend.domain.repository.service.mapper.RepositoriesMapper;
import com.backend.domain.repository.service.mapper.RepositoryInfoMapper;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

import static com.backend.global.exception.ErrorCode.GITHUB_REPO_NOT_FOUND;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryService {

    // 임시
    private final UserRepository userRepository;

    private final GitHubDataFetcher gitHubDataFetcher;
    private final RepositoriesMapper repositoriesMapper;
    private final RepositoryInfoMapper repositoryInfoMapper;
    private final ReadmeInfoMapper readmeInfoMapper;
    private final RepositoryJpaRepository repositoryJpaRepository;

    @Transactional
    public RepositoryData fetchAndSaveRepository(String owner, String repo) {
        try {
            return fetchCompleteRepositoryData(owner, repo);
        } catch (BusinessException e) {
            String errorCode = (e.getErrorCode() != null) ? e.getErrorCode().getCode() : "UNKNOWN";
            log.error("Repository analysis failed for {}/{}: {} - {}",
                    owner, repo, errorCode, e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during repository analysis for {}/{}: {}",
                    owner, repo, e.getMessage(), e);
            throw new BusinessException(ErrorCode.INTERNAL_ERROR);
        }
    }

    @Transactional
    public RepositoryData fetchCompleteRepositoryData(String owner, String repo) {
        RepositoryData data = new RepositoryData();

        // 1. 기본 정보 수집 및 매핑 + Repositories 테이블 저장
        RepoResponse repoInfo = gitHubDataFetcher.fetchRepositoryInfo(owner, repo);
        repositoryInfoMapper.mapBasicInfo(data, repoInfo);

        // TODO: 커밋 데이터 수집 및 매핑


        // TODO: README 데이터 수집 및 매핑
        String readmeInfo = gitHubDataFetcher.fetchReadmeContent(owner, repo);
        readmeInfoMapper.mapReadmeInfo(data, readmeInfo);

        // TODO: 보안 관리 데이터 수집 및 매핑


        // TODO: 테스트 데이터 수집 및 매핑


        // TODO: CI/CD 데이터 수집 및 매핑


        // TODO: 커뮤니티 활성도 데이터 수집 및 매핑

        // Entity 저장 로직
        saveRepositoryEntity(repoInfo);

        log.info("✅ RepositoryData: {}", data);
        return data;
    }

    private void saveRepositoryEntity(RepoResponse repoInfo) {
        User defaultUser = userRepository.findAll().stream()
                .findFirst()
                .orElseThrow(() -> new BusinessException(ErrorCode.INTERNAL_ERROR));

        Repositories entity = repositoriesMapper.toEntity(repoInfo, defaultUser);
        repositoryJpaRepository
                .findByHtmlUrl(entity.getHtmlUrl())
                .map(existing -> {
                    existing.updateFrom(repositoriesMapper.toEntity(repoInfo, defaultUser));
                    return existing;
                })
                .orElseGet(() -> {
                    Repositories newEntity = repositoriesMapper.toEntity(repoInfo, defaultUser);
                    return repositoryJpaRepository.save(newEntity);
                });

        log.info("✅ Repositories: {}", entity);
    }

    // Repository에서 member로 리포지토리 찾기
    public List<Repositories> findRepositoryByMember(Long userId){
        return repositoryJpaRepository.findByUserId(userId);
    }

    // GitRepository ID로 언어 조회
    public List<Language> findLanguagesByRepisotryId(Long gitRepositoryId){
        return repositoryJpaRepository.findLanguagesByRepositoryId(gitRepositoryId);
    }

    // repostiroy 삭제
    public void delete(Long repositoriesId){
        Optional<Repositories> optionalRepository = repositoryJpaRepository.findById(repositoriesId);
        if(optionalRepository.isPresent()){
            Repositories targetRepository = optionalRepository.get();
            repositoryJpaRepository.delete(targetRepository);
        }else{
            throw new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND);
        }
    }
}
