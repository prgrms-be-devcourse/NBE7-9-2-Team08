package com.backend.domain.repository.entity;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.repository.dto.response.github.RepoResponse;
import com.backend.domain.repository.util.LanguageUtils;
import com.backend.domain.user.entity.User;
import com.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Table(
        name = "repositories",
        uniqueConstraints = {
        @UniqueConstraint(
                name = "uk_html_url_user",
                columnNames = {"html_url", "user_id"}
        )
            }
)
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
    private boolean publicRepository = false;

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

    public boolean isPublic() {
        return this.publicRepository;
    }

    public void updatePublicStatus(boolean isPublic) {
        this.publicRepository = isPublic;
    }

    public void updateLanguagesFrom(Map<String, Integer> newLanguagesData) {
        Set<Language> newLanguages = newLanguagesData.keySet().stream()
                .map(LanguageUtils::fromGitHubName)
                .collect(Collectors.toSet());

        Set<Language> existingLanguages = this.languages.stream()
                .map(RepositoryLanguage::getLanguage)
                .collect(Collectors.toSet());

        if (newLanguages.equals(existingLanguages)) {
            return;
        }

        this.languages.removeIf(repoLang ->
                !newLanguages.contains(repoLang.getLanguage()));

        newLanguages.stream()
                .filter(language -> !existingLanguages.contains(language))
                .forEach(language -> {
                    RepositoryLanguage repositoryLanguage = RepositoryLanguage.builder()
                            .repositories(this)
                            .language(language)
                            .build();
                    this.addLanguage(repositoryLanguage);
                });
    }
}
