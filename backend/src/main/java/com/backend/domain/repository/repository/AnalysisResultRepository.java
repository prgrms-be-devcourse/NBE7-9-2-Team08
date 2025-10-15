package com.backend.domain.repository.repository;

import com.backend.domain.repository.entity.AnalysisResult;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisResultRepository extends JpaRepository <AnalysisResult, Long>{
    Page<AnalysisResult> findBymemberId(Long memberId, Pageable pagable);
}
