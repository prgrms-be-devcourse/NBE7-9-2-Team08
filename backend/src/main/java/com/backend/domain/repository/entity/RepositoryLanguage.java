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
    private Repository repository;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Language language;

    public void setRepository(Repository repository) {
        this.repository = repository;
    }

    private RepositoryLanguage(Long id, Repository repository, Language language) {
        this.id = id;
        this.repository = repository;
        this.language = language;
    }
}
