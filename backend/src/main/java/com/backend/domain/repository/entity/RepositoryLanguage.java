package com.backend.domain.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class RepositoryLanguage {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 리포지토리
    @ManyToOne(optional = false)
    @JoinColumn(name = "repository_id", nullable = false)
    private GitRepository gitRepository;

    // 언어 id
    @ManyToOne(optional = false)
    @JoinColumn(name = "language_id", nullable = false)
    private Language language;
}
