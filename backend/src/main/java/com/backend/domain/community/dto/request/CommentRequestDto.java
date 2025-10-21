package com.backend.domain.community.dto.request;

public record CommentRequestDto(
        Long memberId,
        String comment
) {}


