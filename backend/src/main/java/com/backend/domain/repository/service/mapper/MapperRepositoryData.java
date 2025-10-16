package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.RepoResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class MapperRepositoryData {

    public RepositoryData toRepositoryData(RepoResponse response) {
        RepositoryData data = new RepositoryData();
        data.setRepositoryName(response.fullName());
        data.setRepositoryUrl(response.htmlUrl());
        data.setDescription(response.description());
        data.setPrimaryLanguage(response.language());

        ZoneId kst = ZoneId.of("Asia/Seoul");
        LocalDateTime createdAtKST = response.createdAt()
                .atZoneSameInstant(kst)
                .toLocalDateTime();
        data.setRepositoryCreatedAt(createdAtKST);

        return data;
    }

}
