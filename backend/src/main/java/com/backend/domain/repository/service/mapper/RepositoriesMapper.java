package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.github.RepoResponse;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class RepositoriesMapper {
    // 처음 분석하는 리포지토리의 경우 기본 메타 데이터 저장
    public Repositories toEntity(RepoResponse response, User user) {
        return Repositories.builder()
                .user(user)
                .name(response.name())
                .description(response.description())
                .htmlUrl(response.htmlUrl())
                .publicRepository(false)
                .mainBranch(response.defaultBranch())
                .build();
    }
}
