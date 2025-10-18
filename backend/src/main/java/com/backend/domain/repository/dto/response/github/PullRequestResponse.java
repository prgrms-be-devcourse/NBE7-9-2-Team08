package com.backend.domain.repository.dto.response.github;

// Pull Request 응답용 DTO
public record PullRequestResponse(
        Long number,
        String title,
        String state,
        String created_at,
        String merged_at
) {
}
