package com.backend.domain.community.repository;

import com.backend.domain.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    // TODO : QueryDSL로 작성해보기 -> 조회 시 조회 조건(id 순(최신), 점수 순, 좋아요 순 등)
    List<Comment> findByAnalysisResultIdOrderByIdDesc(Long analysisResultId);
    Optional<Comment> findTopByOrderByIdDesc();
}