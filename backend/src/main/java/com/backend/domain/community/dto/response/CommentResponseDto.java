package com.backend.domain.community.dto.response;

import com.backend.domain.community.entity.Comment;
import com.backend.domain.user.entity.User;

import java.time.LocalDateTime;

public record CommentResponseDto(
        Long id,
        Long memberId,
        String name,
        String comment,
        LocalDateTime createDate
) {
    public CommentResponseDto(Comment comment, String userName) {
        this(
                comment.getId(),
                comment.getMemberId(),
                userName,
                comment.getComment(),
                comment.getCreateDate()
        );
    }
}
