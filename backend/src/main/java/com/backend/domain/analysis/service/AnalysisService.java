package com.backend.domain.analysis.service;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.repository.dto.response.RepositoryData;
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

    /* Analysis 분석 프로세스 오케스트레이션 담당
    * 1. GitHub URL 파싱 및 검증
    * 2. Repository 도메인을 통한 데이터 수집
    * 3. Evaluation 도메인을 통한 AI 평가
    * 4. 분석 결과 저장
    * */
    @Transactional
    public void analyze(String githubUrl) {
        String[] repoInfo = parseGitHubUrl(githubUrl);
        String owner = repoInfo[0];
        String repo = repoInfo[1];

        // Repository 데이터 수집
        RepositoryData repositoryData;

        try {
            repositoryData = repositoryService.fetchAndSaveRepository(owner, repo);
            log.info("🫠 Repository Data 수집 완료: {}", repositoryData);
        } catch (BusinessException e) {
            log.error("Repository 데이터 수집 실패: {}/{}", owner, repo, e);
            throw handleRepositoryFetchError(e, owner, repo);
        }

        // TODO: AI 평가
        // EvaluationResult evaluation = evaluationService.evaluate(repositoryData);

        // TODO: AI 평가 저장
    }

    // GitHub URL 파싱하여 owner와 repo 이름 추출
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
}
