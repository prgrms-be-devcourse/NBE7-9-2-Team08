package com.backend.domain.analysis.service;

import com.backend.domain.analysis.dto.response.AnalysisResultResponseDto;
import com.backend.domain.analysis.dto.response.HistoryResponseDto;
import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.lock.RedisLockManager;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.evaluation.service.EvaluationService;
import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.RepositoryResponse;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.domain.repository.service.RepositoryService;
import com.backend.domain.user.util.JwtUtil;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
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
    private final RedisLockManager lockManager;
    private final JwtUtil jwtUtil;

    /* Analysis 분석 프로세스 오케스트레이션 담당
    * 1. GitHub URL 파싱 및 검증
    * 2. Repository 도메인을 통한 데이터 수집
    * 3. Evaluation 도메인을 통한 AI 평가
    * 4. 분석 결과 저장
    * */
    @Transactional
    public Long analyze(String githubUrl, HttpServletRequest request) {
        Long userId = jwtUtil.getUserId(request);
        if (userId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        String[] repoInfo = parseGitHubUrl(githubUrl);
        String owner = repoInfo[0];
        String repo = repoInfo[1];

        String cacheKey = userId + ":" + githubUrl;

        if (!lockManager.tryLock(cacheKey)) {
            throw new BusinessException(ErrorCode.ANALYSIS_IN_PROGRESS);
        }

        try {
            safeSendSse(userId, "status", "분석 시작");

            // Repository 데이터 수집
            RepositoryData repositoryData;

            try {
                repositoryData = repositoryService.fetchAndSaveRepository(owner, repo, userId);
                lockManager.refreshLock(cacheKey);
                safeSendSse(userId, "status", "GitHub 데이터 수집 완료");
                log.info("Repository Data 수집 완료: {}", repositoryData);
            } catch (BusinessException e) {
                log.error("Repository 데이터 수집 실패: {}/{}", owner, repo, e);
                safeSendSse(userId, "error", "Repository 데이터 수집 실패");
                throw new BusinessException(ErrorCode.ANALYSIS_FAIL);
            }

            Repositories savedRepository = repositoryJpaRepository
                    .findByHtmlUrlAndUserId(repositoryData.getRepositoryUrl(), userId)
                    .orElseThrow(() -> new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND));

            Long repositoryId = savedRepository.getId();

            // OpenAI API 데이터 분석 및 저장
            try {
                evaluationService.evaluateAndSave(repositoryData, userId);
                lockManager.refreshLock(cacheKey);
                safeSendSse(userId, "status", "AI 평가 완료");
            } catch (BusinessException e) {
                safeSendSse(userId, "error", "AI 평가 실패: " + e.getMessage());
                throw new BusinessException(ErrorCode.ANALYSIS_FAIL);
            }

            safeSendSse(userId, "complete", "최종 리포트 생성");
            return repositoryId;
        } finally {
            // 락 해제
            lockManager.releaseLock(cacheKey);
            log.info("분석 락 해제: cacheKey={}", cacheKey);
        }
    }

    // SSE 전송 헬퍼 메서드
    private void safeSendSse(Long userId, String event, String message) {
        try {
            sseProgressNotifier.notify(userId, event, message);
        } catch (Exception e) {
            log.warn("SSE 전송 실패 (분석은 계속): userId={}, event={}, error={}",
                    userId, event, e.getMessage());
        }
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

    // 분석 히스토리 조회
    @Transactional(readOnly = true)
    public HistoryResponseDto getHistory(Long repositoryId, HttpServletRequest request) {
        // 1. Repository 조회
        Repositories repository = repositoryJpaRepository.findById(repositoryId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND));

        // 2. 권한 검증 (서비스에서 처리)
        Long requestUserId = jwtUtil.getUserId(request);  // null 가능 (비로그인)
        validateAccess(repository, requestUserId);

        // 3. 분석 결과 조회
        List<AnalysisResult> analysisResults =
                analysisResultRepository.findAnalysisResultByRepositoriesId(repositoryId);

        // 4. DTO 변환
        List<HistoryResponseDto.AnalysisVersionDto> versions = new ArrayList<>();
        int versionNumber = analysisResults.size();

        for (AnalysisResult analysis : analysisResults) {
            versions.add(HistoryResponseDto.AnalysisVersionDto.from(analysis, versionNumber));
            versionNumber--;
        }

        RepositoryResponse repositoryResponse = new RepositoryResponse(repository);
        return HistoryResponseDto.of(repositoryResponse, versions);
    }

    // 분석 상세 조회
    @Transactional(readOnly = true)
    public AnalysisResultResponseDto getAnalysisDetail(Long repositoryId, Long analysisId,
                                                       HttpServletRequest request) {
        // 1. 분석 결과 조회
        AnalysisResult analysisResult = analysisResultRepository.findById(analysisId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_NOT_FOUND));

        // 2. repositoryId 일치 검증
        if (!analysisResult.getRepositories().getId().equals(repositoryId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        // 3. 권한 검증
        Long requestUserId = jwtUtil.getUserId(request);  // null 가능 (비로그인)
        validateAccess(analysisResult.getRepositories(), requestUserId);

        return new AnalysisResultResponseDto(analysisResult, analysisResult.getScore());
    }

    // Repository 삭제
    @Transactional
    public void delete(Long repositoriesId, Long userId, HttpServletRequest request){
        Long requestUserId = jwtUtil.getUserId(request);
        if (requestUserId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!requestUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

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

    // 특정 분석 결과 삭제
    @Transactional
    public void deleteAnalysisResult(Long analysisResultId, Long repositoryId, Long memberId, HttpServletRequest request) {
        Long requestUserId = jwtUtil.getUserId(request);
        if (requestUserId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!requestUserId.equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

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
    public Repositories updatePublicStatus(Long repositoryId, Long memberId, HttpServletRequest request) {
        Long requestUserId = jwtUtil.getUserId(request);
        if (requestUserId == null) {
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        if (!requestUserId.equals(memberId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

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

    // 리포지토리 접근 권한 검증
    private void validateAccess(Repositories repository, Long requestUserId) {
        // 1. 공개 리포지토리는 누구나 접근 가능
        if (repository.isPublicRepository()) {
            log.debug("공개 리포지토리 접근: repoId={}, requestUserId={}",
                    repository.getId(), requestUserId);
            return;
        }

        // 2. 비공개 리포지토리는 로그인 필수
        if (requestUserId == null) {
            log.warn("비로그인 사용자의 비공개 리포지토리 접근 시도: repoId={}",
                    repository.getId());
            throw new BusinessException(ErrorCode.UNAUTHORIZED);
        }

        // 3. 비공개 리포지토리는 소유자만 접근 가능
        Long ownerId = repository.getUser().getId();
        if (!requestUserId.equals(ownerId)) {
            log.warn("권한 없는 사용자의 비공개 리포지토리 접근: requestUserId={}, ownerId={}, repoId={}",
                    requestUserId, ownerId, repository.getId());
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        log.debug("비공개 리포지토리 소유자 접근: userId={}, repoId={}",
                requestUserId, repository.getId());
    }

    // 리포지토리 Url 파싱
    private String[] parseGitHubUrl(String githubUrl) {
        log.info("분석 요청 url: {}", githubUrl);

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

        return new String[]{parts[0].trim(), parts[1].trim()};
    }
}
