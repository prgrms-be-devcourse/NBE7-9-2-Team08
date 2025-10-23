package com.backend.domain.community.dto.response;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;
import com.backend.domain.repository.entity.Repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record CommunityResponseDto(
        String userName,
        String userImage,
        String repositoryName,
        Long repositoryId,
        String summary,
        String description,
        List<String> language,
        int totalScore,
        LocalDateTime createDate,
        boolean vewingStatus
) {
    public CommunityResponseDto(Repositories repositories, AnalysisResult analysis, Score score) {
        this(
                repositories.getUser().getName(),
                repositories.getUser().getImageUrl(),
                repositories.getName(),
                repositories.getId(),
                analysis.getSummary(),
                repositories.getDescription(),
                repositories.getLanguages().stream()
                        .map(language -> language.getLanguage().name())
                        .collect(Collectors.toList()),
                score.getTotalScore(),
                analysis.getCreateDate(),
                repositories.isPublicRepository()
        );
    }
}
