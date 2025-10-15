package com.backend.domain.repository.entity;

import com.backend.domain.user.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;

@Entity
@Getter
public class Repository {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private String htmlUrl;

    @Column(nullable = false)
    private boolean isPublic = false;

    @Column(nullable = false)
    private String mainBranch = "main";
}
