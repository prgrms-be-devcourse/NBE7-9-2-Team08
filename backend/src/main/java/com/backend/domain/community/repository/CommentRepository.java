package com.backend.domain.community.repository;

import com.backend.domain.community.entity.Comment;
import com.backend.domain.repository.entity.Repositories;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CommentRepository extends JpaRepository<Comment, Long> {
}