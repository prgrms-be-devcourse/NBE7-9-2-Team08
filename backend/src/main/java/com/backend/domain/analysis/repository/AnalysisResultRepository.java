package com.backend.domain.analysis.repository;

import com.backend.domain.analysis.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface AnalysisResultRepository extends JpaRepository <AnalysisResult, Long>{
    Optional<AnalysisResult> findByRepositoriesId(Long repositoryId);
    List<AnalysisResult> findAnalysisResultByRepositoriesId(Long repositoryId);
}
