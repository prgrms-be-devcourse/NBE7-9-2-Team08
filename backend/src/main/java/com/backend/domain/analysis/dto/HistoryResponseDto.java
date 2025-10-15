package com.backend.domain.analysis.dto;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.repository.entity.Repository;

import java.time.LocalDateTime;

public record HistoryResponseDto(
    String repositoryName,
    LocalDateTime createDate,
    int totalScore,
    boolean publicStatus
) {
   public HistoryResponseDto(Repository repository, AnalysisResult analysisResult){
       this(
               repository.getName(),
               analysisResult.getCreateDate(),
               analysisResult.getTotalScore(),
               repository.isPublicStatus()
       );
   }
}
