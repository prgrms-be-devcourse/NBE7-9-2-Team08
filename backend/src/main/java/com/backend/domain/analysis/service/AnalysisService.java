package com.backend.domain.analysis.service;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.lock.InMemoryLockManager;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.evaluation.service.EvaluationService;
import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.entity.Language;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.entity.RepositoryLanguage;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.domain.repository.repository.RepositoryLanguageRepository;
import com.backend.domain.repository.service.RepositoryService;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final RepositoryService repositoryService;
    private final AnalysisResultRepository analysisResultRepository;
    private final EvaluationService evaluationService;
    private final RepositoryJpaRepository repositoryJpaRepository;
    private final SseProgressNotifier sseProgressNotifier;
    private final InMemoryLockManager lockManager;
    private final RepositoryLanguageRepository repositoryLanguageRepository;

    /* Analysis 분석 프로세스 오케스트레이션 담당
    * 1. GitHub URL 파싱 및 검증
    * 2. Repository 도메인을 통한 데이터 수집
    * 3. Evaluation 도메인을 통한 AI 평가
    * 4. 분석 결과 저장
    * */
    @Transactional
    public Long analyze(String githubUrl, Long userId) {
        String[] repoInfo = parseGitHubUrl(githubUrl);
        String owner = repoInfo[0];
        String repo = repoInfo[1];

        String cacheKey = userId + ":" + githubUrl;

        if (!lockManager.tryLock(cacheKey)) {
            throw new BusinessException(ErrorCode.ANALYSIS_IN_PROGRESS);
        }

        try {
            sseProgressNotifier.notify(userId, "status", "분석 시작");

            // Repository 데이터 수집
            RepositoryData repositoryData;

            try {
                repositoryData = repositoryService.fetchAndSaveRepository(owner, repo, userId);
                lockManager.refreshLock(cacheKey);
                log.info("🫠 Repository Data 수집 완료: {}", repositoryData);
            } catch (BusinessException e) {
                log.error("Repository 데이터 수집 실패: {}/{}", owner, repo, e);
                throw handleRepositoryFetchError(e, owner, repo);
            }

            Repositories savedRepository = repositoryJpaRepository
                    .findByHtmlUrlAndUserId(repositoryData.getRepositoryUrl(), userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND));

            Long repositoryId = savedRepository.getId();

            // OpenAI API 데이터 분석 및 저장
            try {
                evaluationService.evaluateAndSave(repositoryData, userId);
                lockManager.refreshLock(cacheKey);
            } catch (BusinessException e) {
                sseProgressNotifier.notify(userId, "error", "AI 평가 실패: " + e.getMessage());
                throw e;
            }

            sseProgressNotifier.notify(userId, "complete", "최종 리포트 생성");
            return repositoryId;
        } finally {
            // 락 해제
            lockManager.releaseLock(cacheKey);
            log.info("분석 락 해제: cacheKey={}", cacheKey);
        }
    }

    private String[] parseGitHubUrl(String githubUrl) {
        log.info("🚩 분석 요청 url: {}", githubUrl);

        if (githubUrl == null) {
            throw new BusinessException(ErrorCode.INVALID_GITHUB_URL);
        }

        if (!githubUrl.startsWith("https://github.com/")) {
            throw new BusinessException(ErrorCode.INVALID_GITHUB_URL);
        }

        String path = githubUrl.replace("https://github.com/", "");
        if (path.endsWith("/")) {
            path = path.substring(0, path.length() - 1);
        }

        String[] parts = path.split("/");
        if (parts.length < 2 || parts[0].trim().isEmpty() || parts[1].trim().isEmpty()) {
            throw new BusinessException(ErrorCode.INVALID_REPOSITORY_PATH);
        }

        log.info("🚩 파싱 완료 - owner: '{}', repo: '{}'", parts[0].trim(), parts[1].trim());
        return new String[]{parts[0].trim(), parts[1].trim()};
    }

    // Repository 데이터 수집 중 발생한 예외 처리
    private BusinessException handleRepositoryFetchError(BusinessException e, String owner, String repo) {
        return switch (e.getErrorCode()) {
            case GITHUB_REPO_NOT_FOUND ->
                    new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND);
            case GITHUB_RATE_LIMIT_EXCEEDED ->
                    new BusinessException(ErrorCode.GITHUB_RATE_LIMIT_EXCEEDED);
            default -> e;
        };
    }

    // 특정 Repository의 모든 분석 결과 조회 (최신순)
    public List<AnalysisResult> getAnalysisResultList(Long repositoryId){
        return analysisResultRepository.findAnalysisResultByRepositoriesId(repositoryId);
    }

    // 분석 결과 ID로 단건 조회
    public AnalysisResult getAnalysisById(Long analysisId) {
        return analysisResultRepository.findById(analysisId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_NOT_FOUND));
    }

    // Repository 삭제
    @Transactional
    public void delete(Long repositoriesId, Long userId){
        if (repositoriesId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Repositories targetRepository = repositoryJpaRepository.findById(repositoriesId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND));

        if (!targetRepository.getUser().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        repositoryJpaRepository.delete(targetRepository);
    }

    // repository 사용 언어 반환
    public List<Language> getLanguageByRepositoriesId(Long repositoriesId) {
        return repositoryLanguageRepository.findByRepositories_Id(repositoriesId)
                .stream()
                .map(RepositoryLanguage::getLanguage)
                .toList();
    }

    // 특정 분석 결과 삭제
    @Transactional
    public void deleteAnalysisResult(Long analysisResultId, Long repositoryId, Long memberId) {
        if (analysisResultId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        AnalysisResult analysisResult = analysisResultRepository.findById(analysisResultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_NOT_FOUND));

        if (!analysisResult.getRepositories().getId().equals(repositoryId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        if (!analysisResult.getRepositories().getUser().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        analysisResultRepository.delete(analysisResult);
    }

    // 분석 결과 공개 여부 변경
    @Transactional
    public Repositories updatePublicStatus(Long repositoryId, Long memberId) {
        Repositories repository = repositoryJpaRepository.findById(repositoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND));

        if (!repository.getUser().getId().equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        boolean newStatus = !repository.isPublic();

        if (newStatus) {
            long analysisCount = analysisResultRepository
                    .countByRepositoriesId(repositoryId);

            if (analysisCount == 0) {
                throw new BusinessException(ErrorCode.ANALYSIS_NOT_FOUND);
            }
        }

        repository.updatePublicStatus(newStatus);
        return repository;
    }
}
