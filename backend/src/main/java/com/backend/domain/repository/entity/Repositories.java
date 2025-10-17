package com.backend.domain.repository.entity;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.user.entity.User;
import com.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repositories")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Repositories extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @OneToMany(mappedBy = "repositories", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<AnalysisResult> analysisResults = new ArrayList<>();

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String htmlUrl;

    @Column(name = "public_repository")
    private boolean publicRepository;

    @Column(name = "main_branch")
    private String mainBranch;

    @OneToMany(mappedBy = "repositories", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepositoryLanguage> languages = new ArrayList<>();

    @Builder
    public Repositories(
            String name,
            String description,
            String htmlUrl,
            boolean publicRepository,
            String mainBranch,
            List<RepositoryLanguage> languages
    ) {
        this.name = name;
        this.description = description;
        this.htmlUrl = htmlUrl;
        this.publicRepository = publicRepository;
        this.mainBranch = mainBranch;

        if (languages != null) {
            languages.forEach(this::addLanguage);
        }
    }

    public void addLanguage(RepositoryLanguage language) {
        this.languages.add(language);
        language.setRepositories(this);
    }

    public void updateFrom(Repositories other) {
        this.name = other.name;
        this.description = other.description;
        this.mainBranch = other.mainBranch;
        this.publicRepository = other.publicRepository;
    }
}
