package com.backend.domain.repository.dto.response.github;

import java.util.List;

// trees 응답용 DTO
public record TreeResponse(
        List<TreeItem> tree,
        boolean truncated // 응답이 잘렸을 경우 true
) {
    public record TreeItem(
            String path,
            String type
    ) {}
}
