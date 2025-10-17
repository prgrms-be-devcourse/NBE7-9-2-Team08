package com.backend.domain.analysis.dto;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.entity.RepositoryLanguage;

import java.time.LocalDateTime;
import java.util.List;

public record HistoryResponseDto(
    String repositoryName,
    LocalDateTime createDate,
    List<String> languages,
    int totalScore,
    boolean publicStatus
) {
   public HistoryResponseDto(Repositories repositories, AnalysisResult analysisResult, Score score, List<String> languages){
       this(
               repositories.getName(),
               analysisResult.getCreateDate(),
               languages,
               score.getTotalScore(),
               repositories.isPublicRepository()
       );
   }
}
