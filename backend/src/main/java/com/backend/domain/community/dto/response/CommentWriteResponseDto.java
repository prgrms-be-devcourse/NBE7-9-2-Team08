package com.backend.domain.community.dto.response;

import com.backend.domain.community.entity.Comment;

import java.time.LocalDateTime;

public record CommentWriteResponseDto(
        Long id,
        Long memberId,
        String comment,
        LocalDateTime createDate
){
    public CommentWriteResponseDto(Comment comment) {
        this(
                comment.getId(),
                comment.getMemberId(),
                comment.getComment(),
                comment.getCreateDate()
        );
    }
}