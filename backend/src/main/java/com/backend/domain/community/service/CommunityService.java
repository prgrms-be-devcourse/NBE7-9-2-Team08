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

    // publicRepository(repository 필드)가 true인 리포지토리 조회
    public List<Repositories> getRepositoriesPublicTrue(){
        return repositoryJpaRepository.findByPublicRepository(true);
    }

    // 댓글 추가
    public Comment addComment(Long analysisResultId, Long memberId, String content) {
        AnalysisResult analysisResult = analysisResultRepository.findById(analysisResultId)
                .orElseThrow(() -> new BusinessException(ErrorCode.ANALYSIS_NOT_FOUND));

        Comment comment = Comment.builder()
                .analysisResult(analysisResult)
                .memberId(memberId)
                .comment(content)
                .build();

        return commentRepository.save(comment);
    }

    // 특정 분석 결과의 댓글 조회
    public List<Comment> getCommentsByAnalysisResult(Long analysisResultId) {
        return commentRepository.findByAnalysisResult_Id(analysisResultId);
    }

}
