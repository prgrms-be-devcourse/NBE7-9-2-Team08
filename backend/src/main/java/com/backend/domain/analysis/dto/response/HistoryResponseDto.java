package com.backend.domain.analysis.dto.response;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.repository.dto.response.RepositoryResponse;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Repository 상세 정보 + 분석 버전 목록 응답 DTO
 * 특정 Repository의 모든 분석 버전을 조회할 때 사용
 */
public record HistoryResponseDto(
        RepositoryResponse repository,           // Repository 기본 정보
        List<AnalysisVersionDto> analysisVersions
) {
    public static HistoryResponseDto of(
            RepositoryResponse repository,
            List<AnalysisVersionDto> versions
    ) {
        return new HistoryResponseDto(repository, versions);
    }

    public record AnalysisVersionDto(
            Long analysisId,
            LocalDateTime analysisDate,
            Integer totalScore,
            String versionLabel
    ) {
        public static AnalysisVersionDto from(
                AnalysisResult analysis,
                int versionNumber
        ) {
            return new AnalysisVersionDto(
                    analysis.getId(),
                    analysis.getCreateDate(),
                    analysis.getScore().getTotalScore(),
                    String.format("v%d (%s)",
                            versionNumber,
                            analysis.getCreateDate().toLocalDate())
            );
        }
    }
}
