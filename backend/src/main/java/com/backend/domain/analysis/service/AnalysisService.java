package com.backend.domain.analysis.service;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.repository.service.RepositoryService;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AnalysisService {
    private final RepositoryService repositoryService;
    private final AnalysisResultRepository analysisResultRepository;

    @Transactional
    public void analyze(String githubUrl) {
        if(githubUrl == null || !githubUrl.startsWith("https://github.com/")) {
            throw new BusinessException(ErrorCode.INVALID_GITHUB_URL);
        }

        String[] parts = githubUrl.replace("https://github.com/", "").split("/");
        if(parts.length < 2){
            throw new BusinessException(ErrorCode.INVALID_REPOSITORY_PATH);
        }

        String owner = parts[0];
        String repo = parts[1];

        // GitHub API 호출
        repositoryService.fetchAndSaveRepository(owner, repo);
    }

    // AnalysisRresult에서 repository id로 분석 결과 찾기
    public Optional<AnalysisResult> findAnalysisResultByRepositoryId (Long RepositoryId){
        return analysisResultRepository.findByRepositories_Id(RepositoryId);
    }
}
