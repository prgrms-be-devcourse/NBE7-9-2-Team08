package com.backend.domain.repository.entity;

import com.backend.global.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "repository")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Repository extends BaseEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

//    @ManyToOne(fetch = FetchType.LAZY)
//    @JoinColumn(name = "user_id", nullable = false)
//    private Users users;

    @Column(nullable = false)
    private String name;

    @Column(length = 500)
    private String description;

    @Column(nullable = false)
    private String htmlUrl;

    @Column(nullable = false)
    private String defaultBranch;

    @Column(name = "is_public")
    private boolean isPublic;

    @Column(name = "main_branch")
    private String mainBranch;

    @OneToMany(mappedBy = "repository", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<RepositoryLanguage> languages = new ArrayList<>();
}
