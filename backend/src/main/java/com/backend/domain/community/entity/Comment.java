package com.backend.domain.community.entity;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment extends BaseEntity {
    // 댓글 id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 분석결과 id
    @ManyToOne(optional= false)
    @JoinColumn(name = "analysis_result_id",  nullable = false)
    private AnalysisResult analysisResult;

    // 회원 id
    @Column(name = "member_id", nullable = false)
    private Long memberId;

    // 댓글 내용
    @Column(columnDefinition = "TEXT", nullable = false)
    private String comment;

    public void setAnalysisResult(AnalysisResult analysisResult) {
        this.analysisResult = analysisResult;
        analysisResult.getComments().add(this);
    }
}
