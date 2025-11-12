package com.backend.domain.community.service;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.community.entity.Comment;
import com.backend.domain.community.repository.CommentRepository;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Builder
public class CommunityService {
    private final RepositoryJpaRepository repositoryJpaRepository;
    private final AnalysisResultRepository analysisResultRepository;
    private final CommentRepository commentRepository;


    // 커뮤니티 - 리포지토리 조회
    // publicRepository(repository 필드)가 true인 리포지토리 조회
    public List<Repositories> getRepositoriesPublicTrue(){
        return repositoryJpaRepository.findByPublicRepository(true);
    }

    // 공개 여부 true인 Repository 페이징 조회
    public Page<Repositories> getPagedRepositoriesPublicTrue(int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("createDate").descending());
        return repositoryJpaRepository.findByPublicRepositoryTrue(pageable);
    }

    // 댓글 추가
    public Comment addComment(Long analysisResultId, Long memberId, String content) {
        AnalysisResult analysisResult = analysisResultRepository.findById(analysisResultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_NOT_FOUND));

        if (content == null || content.trim().isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_COMMENT);
        }

        Comment comment = Comment.builder()
                .analysisResult(analysisResult)
                .memberId(memberId)
                .comment(content)
                .build();

        return commentRepository.save(comment);
    }

    // 댓글 조회
    public List<Comment> getCommentsByAnalysisResult(Long analysisResultId) {
        // id 내림차순으로 정렬된 댓글 리스트 반환
        return commentRepository.findByAnalysisResultIdAndDeletedOrderByIdDesc(analysisResultId, false);
    }

    // 댓글 조회 - 페이징 추가
    public Page<Comment> getPagedCommentsByAnalysisResult(Long analysisResultId, int page, int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("id").descending());
        return commentRepository.findByAnalysisResultIdAndDeletedOrderByIdDesc(analysisResultId, false, pageable);
    }

    // ✅ 댓글 삭제 (본인만 가능)
    public void deleteComment(Long commentId, Long jwtUserId) {
        if (commentId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Comment targetComment = commentRepository.findByIdAndDeleted(commentId, false)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 🔒 작성자 본인 확인
        if (!targetComment.getMemberId().equals(jwtUserId)) {
            throw new BusinessException(ErrorCode.NOT_WRITER); // 권한 없음
        }

        commentRepository.delete(targetComment);
    }

    @Transactional // ✅ 트랜잭션 readOnly=false로 override
    public void modifyComment(Long commentId, String newContent, Long jwtUserId) {
        Comment targetComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        // 🔒 작성자 본인 확인
        if (!targetComment.getMemberId().equals(jwtUserId)) {
            throw new BusinessException(ErrorCode.NOT_WRITER);
        }

        if (newContent == null || newContent.isEmpty()) {
            throw new BusinessException(ErrorCode.EMPTY_COMMENT);
        }

        targetComment.updateComment(newContent); // ✅ 엔티티 변경 감지
    }
}
