package com.backend.domain.analysis.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "score")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Score {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "analysis_result_id", nullable = false)
    private AnalysisResult analysisResult;

    @Column(nullable = false)
    private int readmeScore;

    @Column(nullable = false)
    private int testScore;

    @Column(nullable = false)
    private int commitScore;

    @Column(nullable = false)
    private int cicdScore;

    // 종합 점수
    @Transient
    public int getTotalScore() {
        return readmeScore + testScore + commitScore + cicdScore;
    }

    @Builder
    public Score(AnalysisResult analysisResult, int readmeScore,
                 int testScore, int commitScore, int cicdScore) {
        this.analysisResult = analysisResult;
        if (analysisResult != null) {
            analysisResult.assignScore(this);
        }

        this.readmeScore = readmeScore;
        this.testScore = testScore;
        this.commitScore = commitScore;
        this.cicdScore = cicdScore;
    }
}
