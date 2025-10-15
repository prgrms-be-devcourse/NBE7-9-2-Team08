package com.backend.domain.repository.dto.response;

import com.backend.domain.repository.entity.Repository;

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
    public RepositoryResponse(Repository repository) {
        this(
                repository.getId(),
                repository.getName(),
                repository.getDescription(),
                repository.getHtmlUrl(),
                repository.isPublicRepository(),
                repository.getMainBranch(),
                repository.getLanguages().stream()
                        .map(lang -> lang.getLanguage().name())
                        .collect(Collectors.toList())
        );
    }
}
