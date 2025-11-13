package com.backend.domain.repository.dto.response;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.repository.entity.Repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record RepositoryComparisonResponse(
        Long repositoryId,
        String name,
        String htmlUrl,
        List<String> languages,
        AnalysisInfo latestAnalysis
) {
    public record AnalysisInfo(
            Long analysisId,
            LocalDateTime analyzedAt,
            CategoryScores scores
    ) {
        public static AnalysisInfo from(AnalysisResult analysis) {
            return new AnalysisInfo(
                    analysis.getId(),
                    analysis.getCreateDate(),
                    new CategoryScores(
                            analysis.getScore().getReadmeScore(),
                            analysis.getScore().getTestScore(),
                            analysis.getScore().getCommitScore(),
                            analysis.getScore().getCicdScore(),
                            analysis.getScore().getTotalScore()
                    )
            );
        }
    }

    public record CategoryScores(
            int readme,
            int test,
            int commit,
            int cicd,
            int total
    ) {}

    public static RepositoryComparisonResponse from(
            Repositories repository,
            AnalysisResult latestAnalysis
    ) {
        return new RepositoryComparisonResponse(
                repository.getId(),
                repository.getName(),
                repository.getHtmlUrl(),
                repository.getLanguages().stream()
                        .map(lang -> lang.getLanguage().name())
                        .collect(Collectors.toList()),
                AnalysisInfo.from(latestAnalysis)
        );
    }
}
