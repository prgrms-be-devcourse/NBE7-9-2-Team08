package com.backend.domain.analysis.entity;

import com.backend.domain.repository.entity.GitRepository;
import com.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;


@Entity
@Getter
public class AnalysisResult extends BaseEntity {
    // 분석결과 id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 리포지토리 id
    @OneToOne(optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    private GitRepository gitRepository;

    // 개요 varchar(225) 기본 길이
    @Column(nullable = false)
    private String summary;

    // 장점 varchar(225) 기본 길이
    @Column(nullable = false)
    private String strengths;

    // 개선점 varchar(225) 기본 길이
    @Column(nullable = false)
    private String improvements;

    // README 점수
    @Column(name = "readme_score")
    private int readmeScore = 0;

    // TEST 점수
    @Column(name = "test_score")
    private int testScore = 0;

    // COMMIT 점수
    @Column(name = "commit_score")
    private int commitScore = 0;

    // CI/CD 점수
    @Column(name = "cicd_score")
    private int ciCdScore = 0;

    // 종합 점수
    @Transient
    public int getTotalScore() {
        return readmeScore + testScore + commitScore + ciCdScore;
    }
}
