package com.backend.domain.repository.entity;

import com.backend.domain.user.entity.Member;
import jakarta.persistence.*;
import lombok.Getter;

// open api 전달 내용 포함 된 entity pr 올라가 있음 -> 추후 수정 필요
@Entity
@Getter
@Table(name = "git_epository")
public class GitRepository {
    // 리포지토리 id
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 회원 id
    @ManyToOne
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    // 리포지토리 이름
    @Column(nullable = false)
    private String name;

    // 리포지토리 설명
    @Column(nullable = false)
    private String description;

    // 리포지토리 url
    @Column(nullable = false)
    private String htmlUrl;

    // 리포지토리 공개 여부
    @Column(name = "isPublic", nullable = false)
    private boolean publicStatus = false;

    // 메인 브랜치
    @Column(nullable = false)
    private String mainBranch = "main";
}
