package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.github.RepoResponse;
import com.backend.domain.repository.entity.Repositories;
import org.springframework.stereotype.Component;

@Component
public class MapperRepositories {

    public Repositories toEntity(RepoResponse response) {
        return Repositories.builder()
                .name(response.name())
                .description(response.description())
                .htmlUrl(response.htmlUrl())
                .publicRepository(!response._private())
                .mainBranch(response.defaultBranch())
                .build();
    }
}
