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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@AllArgsConstructor
@Transactional(readOnly = true)
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
    // TODO : 페이징 추가 예정
    public List<Comment> getCommentsByAnalysisResult(Long analysisResultId) {
        // id 내림차순으로 정렬된 댓글 리스트 반환
        return commentRepository.findByAnalysisResultIdOrderByIdDesc(analysisResultId);
    }

    // 댓글 삭제
    // TODO : 소프트 딜리트 추가 예정
    public void deleteComment(Long commentId){
        if(commentId == null){
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Comment targetComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        commentRepository.delete(targetComment);
    }

    // 댓글 수정
    public void modifyComment(Long commentId, String newContent){

        Comment targetComment = commentRepository.findById(commentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if(newContent == null || newContent.isEmpty()){
            throw new BusinessException(ErrorCode.EMPTY_COMMENT);
        }

        targetComment.updateComment(newContent);
    }
}
