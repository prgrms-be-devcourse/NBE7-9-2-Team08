package com.backend.domain.repository.dto.response;

import com.backend.domain.repository.entity.Repositories;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Repository 기본 정보 응답 DTO
 * 사용자의 Repository 목록 조회 시 사용
 */
public record RepositoryResponse(
        Long id,
        String name,
        String description,
        String htmlUrl,
        boolean publicRepository,
        String mainBranch,
        List<String> languages,
        LocalDateTime createDate,
        Long ownerId
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
                        .collect(Collectors.toList()),
                repositories.getCreateDate(),
                repositories.getUser().getId()
        );
    }
}
