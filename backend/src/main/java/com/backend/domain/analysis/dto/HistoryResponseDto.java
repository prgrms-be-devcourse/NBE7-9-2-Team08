package com.backend.domain.analysis.dto;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.repository.entity.GitRepository;

import java.time.LocalDateTime;

public record HistoryResponseDto(
    String repositoryName,
    LocalDateTime createDate,
    int totalScore,
    boolean publicStatus
) {
   public HistoryResponseDto(GitRepository gitRepository, AnalysisResult analysisResult){
       this(
               gitRepository.getName(),
               analysisResult.getCreateDate(),
               analysisResult.getTotalScore(),
               gitRepository.isPublicStatus()
       );
   }
}
