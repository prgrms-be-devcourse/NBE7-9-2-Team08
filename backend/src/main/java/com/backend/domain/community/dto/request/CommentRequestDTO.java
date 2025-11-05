package com.backend.domain.community.dto.request;

public record CommentRequestDTO(
        Long memberId,
        String comment
) {}


