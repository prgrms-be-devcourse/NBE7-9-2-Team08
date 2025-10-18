package com.backend.domain.repository.dto.response.github;

// commits 응답용 DTO
public record CommitResponse(
        CommitDetails commit
) {
    public record CommitDetails(
            String message,
            AuthorDetails author
    ) {}

    public record AuthorDetails(
            String date
    ) {}
}
