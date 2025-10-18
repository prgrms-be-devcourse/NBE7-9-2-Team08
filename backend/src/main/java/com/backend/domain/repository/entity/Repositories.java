package com.backend.domain.repository.entity;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.repository.dto.response.github.RepoResponse;
import com.backend.domain.repository.service.mapper.RepositoriesMapper;
import com.backend.domain.repository.util.LanguageUtils;
import com.backend.domain.user.entity.User;
import com.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.*;
import java.util.stream.Collectors;

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
            User user,
            String name,
            String description,
            String htmlUrl,
            boolean publicRepository,
            String mainBranch,
            List<RepositoryLanguage> languages
    ) {
        this.user = user;
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

    public void updateFrom(RepoResponse repoInfo) {
        this.name = repoInfo.name();
        this.description = repoInfo.description();
        this.mainBranch = repoInfo.defaultBranch();
    }

    public void updatePublicFrom(Repositories other) {
        this.publicRepository = other.publicRepository;
    }

    public static Repositories createOrUpdateRepositories(Optional<Repositories> existing,
                                                   RepoResponse repoInfo,
                                                   User user,
                                                   RepositoriesMapper mapper) {
        return existing
                .map(repo -> {
                    repo.updateFrom(repoInfo);
                    return repo;
                })
                .orElseGet(() -> mapper.toEntity(repoInfo, user));
    }

    public void updateLanguagesFrom(Map<String, Integer> newLanguagesData) {
        Set<String> newLanguageNames = newLanguagesData.keySet();
        Set<String> existingLanguageNames = this.languages.stream()
                .map(rl -> rl.getLanguage().name())
                .collect(Collectors.toSet());

        if (newLanguageNames.equals(existingLanguageNames)) {
            return;
        }

        this.languages.removeIf(repoLang ->
                !newLanguageNames.contains(repoLang.getLanguage().name()));

        newLanguageNames.stream()
                .filter(langName -> !existingLanguageNames.contains(langName))
                .forEach(langName -> {
                    Language language = LanguageUtils.fromGitHubName(langName);
                    RepositoryLanguage repositoryLanguage = RepositoryLanguage.builder()
                            .repositories(this)
                            .language(language)
                            .build();
                    this.addLanguage(repositoryLanguage);
                });
    }
}
