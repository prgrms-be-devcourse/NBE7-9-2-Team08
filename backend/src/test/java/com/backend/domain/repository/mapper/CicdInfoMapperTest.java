package com.backend.domain.repository.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.TreeResponse;
import com.backend.domain.repository.service.mapper.CicdInfoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CicdInfoMapperTest {

    private final CicdInfoMapper mapper = new CicdInfoMapper();

    @Test
    @DisplayName("TreeResponse가 null이어도 NPE 없이 처리")
    void mapNullTree_shouldHandleGracefully() {
        RepositoryData data = new RepositoryData();
        mapper.mapCicdInfo(data, null);
        assertThat(data.isHasCICD()).isFalse();
    }

    @Test
    @DisplayName("다양한 CI/CD 설정 파일들이 감지되어야 함")
    void mapVariousCicdFiles_shouldBeDetected() {
        RepositoryData data = new RepositoryData();
        TreeResponse tree = new TreeResponse(
                List.of(
                        new TreeResponse.TreeItem(".github/workflows/ci.yml", "blob"),
                        new TreeResponse.TreeItem(".gitlab-ci.yml", "blob"),
                        new TreeResponse.TreeItem("Jenkinsfile", "blob"),
                        new TreeResponse.TreeItem(".circleci/config.yml", "blob")
                ),
                false
        );
        mapper.mapCicdInfo(data, tree);
        assertThat(data.isHasCICD()).isTrue();
        assertThat(data.getCicdFiles()).hasSize(4);
    }

    @Test
    @DisplayName("DockerFile(대소문자 혼합)도 감지되어야 함")
    void dockerfileCaseInsensitive_shouldBeDetected() {
        RepositoryData data = new RepositoryData();
        TreeResponse tree = new TreeResponse(
                List.of(new TreeResponse.TreeItem("infra/DockerFile", "blob")), false
        );
        mapper.mapCicdInfo(data, tree);
        assertThat(data.isHasDockerfile()).isTrue();
    }
}
