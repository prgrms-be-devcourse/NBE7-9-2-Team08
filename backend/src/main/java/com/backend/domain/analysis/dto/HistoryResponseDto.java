package com.backend.domain.analysis.dto;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.repository.entity.GitRepository;
import com.backend.domain.repository.entity.Language;

import java.time.LocalDateTime;
import java.util.List;

public record HistoryResponseDto(
    String repositoryName,
    LocalDateTime createDate,
    List<String> languages,
    int totalScore,
    boolean publicStatus
) {
   public HistoryResponseDto(GitRepository gitRepository, AnalysisResult analysisResult, List<Language> langs){
       this(
               gitRepository.getName(),
               analysisResult.getCreateDate(),
               langs.stream().map(Language::getLanguage).toList(),
               analysisResult.getTotalScore(),
               gitRepository.isPublicStatus()
       );
   }
}
