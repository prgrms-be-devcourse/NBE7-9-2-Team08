package com.backend.domain.repository.dto.response;

import com.backend.domain.repository.entity.Repositories;

import java.util.List;
import java.util.stream.Collectors;

public record RepositoryResponse(
        Long id,
        String name,
        String description,
        String htmlUrl,
        boolean publicRepository,
        String mainBranch,
        List<String> languages
) {
    public RepositoryResponse(Repositories repositories) {
        this(
                repositories.getId(),
                repositories.getName(),
                repositories.getDescription(),
                repositories.getHtmlUrl(),
                repositories.isPublicRepository(),
                repositories.getMainBranch(),
                repositories.getLanguages().stream()
                        .map(lang -> lang.getLanguage().name())
                        .collect(Collectors.toList())
        );
    }
}
