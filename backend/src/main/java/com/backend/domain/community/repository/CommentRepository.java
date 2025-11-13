package com.backend.domain.community.repository;

import com.backend.domain.community.entity.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // 댓글 내용 조회
    List<Comment> findByAnalysisResultIdAndDeletedOrderByIdDesc(Long analysisResultId, boolean deleted);
    Page<Comment> findByAnalysisResultIdAndDeletedOrderByIdDesc(Long analysisResultId, boolean deleted, Pageable pageable);


    // 댓글 작성 테스트에서 가장 최신 댓글을 확인하기 위해서 작성
    Optional<Comment> findTopByOrderByIdDesc();

    // 댓글 삭제 시, 존재하는 댓글인지 확인 하기 위해서 먼저 조회
    Optional<Comment> findByIdAndDeleted(Long commentId, boolean deleted);
}