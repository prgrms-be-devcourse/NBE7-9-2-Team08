package com.backend.domain.analysis.repository;

import com.backend.domain.analysis.entity.AnalysisResult;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisResultRepository extends JpaRepository <AnalysisResult, Long>{
}
