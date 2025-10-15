package com.backend.domain.community.entity;

import com.backend.domain.repository.entity.AnalysisResult;
import com.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Comment extends BaseEntity {
    // 댓글 id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 분석결과 id
    @ManyToOne
    @JoinColumn(name = "analysis_result_id")
    private AnalysisResult analysisResultId;

    // 회원 id
    @Column(name = "member_id")
    private Long memberId;

    // 댓글 내용
    @Column(columnDefinition = "TEXT")
    private String comment;
}
