package com.backend.domain.analysis.service;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.repository.service.RepositoryService;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final RepositoryService repositoryService;
    private final AnalysisResultRepository analysisResultRepository;

    @Transactional
    public void analyze(String githubUrl) {
        String[] repoInfo = parseGitHubUrl(githubUrl);
        String owner = repoInfo[0];
        String repo = repoInfo[1];

        // Repository 데이터 수집
        RepositoryData repositoryData = repositoryService.fetchAndSaveRepository(owner, repo);

        // TODO: AI 평가
        // EvaluationResult evaluation = evaluationService.evaluate(repositoryData);

        // TODO: AI 평가 저장
    }

    private String[] parseGitHubUrl(String githubUrl) {
        log.info("🚩 분석 요청 url: {}", githubUrl);

        if (githubUrl == null || !githubUrl.startsWith("https://github.com/")) {
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

    // AnalysisRresult에서 repository id로 분석 결과 찾기
    public Optional<AnalysisResult> findAnalysisResultByRepositoryId (Long RepositoryId){
        return analysisResultRepository.findByRepositoriesId(RepositoryId);
    }
}
