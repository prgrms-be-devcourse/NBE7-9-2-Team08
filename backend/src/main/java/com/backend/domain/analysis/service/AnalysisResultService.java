package com.backend.domain.analysis.service;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AnalysisResultService {
    private final AnalysisResultRepository analysisResultRepository;

    // AnalysisRresult에서 repository id로 분석 결과 찾기
    public Optional<AnalysisResult> findAnalysisResultByRepositoryId (Long gitRepositoryId){
        return analysisResultRepository.findByGitRepository_Id(gitRepositoryId);
    }
}
