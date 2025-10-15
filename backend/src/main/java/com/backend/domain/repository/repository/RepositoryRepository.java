package com.backend.domain.repository.repository;

import com.backend.domain.analysis.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface RepositoryRepository extends JpaRepository <AnalysisResult, Long>{
    List<com.backend.domain.repository.entity.Repository> findByMemberId(Long memberId);
}
