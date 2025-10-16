package com.backend.domain.repository.entity;

import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Language {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String language;
}
