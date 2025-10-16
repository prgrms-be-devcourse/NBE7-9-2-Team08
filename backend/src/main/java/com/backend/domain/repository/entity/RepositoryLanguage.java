package com.backend.domain.repository.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "repository_language")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
public class RepositoryLanguage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "repository_id", nullable = false)
    private Repositories repositories;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    public void setRepositories(Repositories repositories) {
        this.repositories = repositories;
    }

    private RepositoryLanguage(Long id, Repositories repositories, Language language) {
        this.id = id;
        this.repositories = repositories;
        this.language = language;
    }
}
