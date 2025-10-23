package com.backend.domain.analysis.dto.response;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;

/**
 * 특정 분석 결과의 상세 정보 응답 DTO
 * 분석 점수, 피드백 등을 포함
 */
public record AnalysisResultResponseDto(
    int totalScore,
    int readmeScore,
    int testScore,
    int commitScore,
    int cicdScore,
    String summary,
    String strengths,
    String improvements
) {
    public AnalysisResultResponseDto(AnalysisResult analysisResult, Score score){
        this(
                score.getTotalScore(),
                score.getReadmeScore(),
                score.getTestScore(),
                score.getCommitScore(),
                score.getCicdScore(),
                analysisResult.getSummary(),
                analysisResult.getStrengths(),
                analysisResult.getImprovements()
        );
    }
}
