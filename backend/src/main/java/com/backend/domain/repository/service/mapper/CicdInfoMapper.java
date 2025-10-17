package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.TreeResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;

@Component
public class CicdInfoMapper {
    // ResponseData CI/CD 관련 [CI/CD 존재 여부 관련]
    public void mapCicdInfo(RepositoryData data, TreeResponse response) {
        if (response == null || response.tree().isEmpty()) {
            setEmptyCicdData(data);
            return;
        }
    }

    private void setEmptyCicdData(RepositoryData data) {
        data.setHasCICD(false);
        data.setCicdFiles(Collections.emptyList());
        data.setHasDockerfile(false);
    }
}
