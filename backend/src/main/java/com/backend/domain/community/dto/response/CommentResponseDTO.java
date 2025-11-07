package com.backend.domain.community.dto.response;

import com.backend.domain.community.entity.Comment;

import java.time.LocalDateTime;

public record CommentResponseDTO(
        Long commentId,
        Long memberId,
        String name,
        String comment,
        LocalDateTime createDate
) {
    public CommentResponseDTO(Comment comment, String userName) {
        this(
                comment.getId(),
                comment.getMemberId(),
                userName,
                comment.getComment(),
                comment.getCreateDate()
        );
    }
}
