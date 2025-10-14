package com.backend.domain.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class AnalysisResultLanguage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 분석결과 id
    @ManyToOne
    @JoinColumn(name = "analysis_result_id")
    private AnalysisResult analysisId;

    // 언어 id
    @ManyToOne
    @JoinColumn(name = "language_id")
    private Language languageId;
}
