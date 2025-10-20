package com.backend.domain.analysis.entity;

import com.backend.domain.community.entity.Comment;
import com.backend.domain.repository.entity.Repositories;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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

    @Column(nullable = false)
    private String summary;

    @Column(nullable = false)
    private String strengths;

    @Column(nullable = false)
    private String improvements;

    @Column(nullable = false, name = "createData")
    private LocalDateTime createDate;

    @OneToOne(mappedBy = "analysisResult", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Score score;

    @OneToMany(mappedBy = "analysisResultId", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();
}
