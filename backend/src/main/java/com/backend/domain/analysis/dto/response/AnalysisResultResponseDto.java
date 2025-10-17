package com.backend.domain.analysis.dto.response;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;

public record AnalysisResultResponseDto(
    int totalScore,
    int readmeScore,
    int testScore,
    int commitScore,
    int cicdScore,
    String summery,
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
