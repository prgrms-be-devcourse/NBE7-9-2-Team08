package com.backend.domain.analysis.entity;

import com.backend.domain.repository.entity.Repositories;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table (name = "analysis_result")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class AnalysisResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repositories repositories;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String summary;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String strengths;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String improvements;

    @Column(nullable = false, name = "createData")
    private LocalDateTime createDate;

    @OneToOne(mappedBy = "analysisResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Score score;

    @Builder
    public AnalysisResult(Repositories repositories, String summary,
                          String strengths, String improvements, LocalDateTime createDate) {
        this.repositories = repositories;
        this.summary = summary;
        this.strengths = strengths;
        this.improvements = improvements;
        this.createDate = createDate;
    }

    public void assignScore(Score score) {
        this.score = score;
    }
}
