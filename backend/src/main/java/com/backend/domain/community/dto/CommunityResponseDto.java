package com.backend.domain.community.dto;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.user.entity.User;

import java.util.List;
import java.util.stream.Collectors;

public record CommunityResponseDto(
    String userName,
    String repositoryName,
    String summary,
    List<String> language,
    int totalScore
) {
    public CommunityResponseDto(User user, Repositories repositories, AnalysisResult analysis, Score score){
        this(
                user.getName(),
                repositories.getName(),
                analysis.getSummary(),
                repositories.getLanguages().stream()
                        .map(language -> language.getLanguage().name())
                        .collect(Collectors.toList()),
                score.getTotalScore()
        );
    }
}
