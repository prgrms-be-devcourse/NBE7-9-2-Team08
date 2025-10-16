package com.backend.domain.repository.dto.response.github;

import com.fasterxml.jackson.annotation.JsonProperty;

import java.time.OffsetDateTime;

public record RepoResponse(
        String name,
        @JsonProperty("full_name") String fullName,
        boolean _private,
        String description,
        @JsonProperty("html_url") String htmlUrl,
        String language,
        @JsonProperty("default_branch") String defaultBranch,
        @JsonProperty("created_at") OffsetDateTime createdAt
) {
}
