package com.backend.domain.repository.entity;

import com.backend.domain.user.entity.Member;
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

    // 회원 id
    @ManyToOne
    @JoinColumn(name = "member_id")
    private Member memberId;

    // 리포지토리 이름
    @Column(name = "resptoty_name")
    private String repositoryName;

    // 개요 varchar(225) 기본 길이
    @Column
    private String summery;

    // 장점 varchar(225) 기본 길이
    @Column
    private String strengths;

    // 개선점 varchar(225) 기본 길이
    @Column
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
}
