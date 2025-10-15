package com.backend.domain.repository.repository;

import com.backend.domain.repository.entity.AnalysisResultLanguage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AnalysisResultLanguageRepository extends JpaRepository <AnalysisResultLanguage, Long>{
}
