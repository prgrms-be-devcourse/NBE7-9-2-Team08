package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.github.RepoResponse;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.user.entity.User;
import org.springframework.stereotype.Component;

@Component
public class RepositoriesMapper {

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
