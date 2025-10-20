package com.backend.domain.community.repository;

import com.backend.domain.community.dto.request.CommentUpdateRequestDto;
import com.backend.domain.community.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByAnalysisResult_Id(Long analysisResultId);
}