package com.backend.domain.repository.dto.response.github;

import com.fasterxml.jackson.annotation.JsonProperty;

// readme 응답용 DTO
public record ReadmeResponse(
        String name,
        String path,
        String sha,
        Integer size,
        String url,
        @JsonProperty("html_url") String htmlUrl,
        @JsonProperty("download_url") String downloadUrl,
        String type,
        String content,
        String encoding
) {
}
