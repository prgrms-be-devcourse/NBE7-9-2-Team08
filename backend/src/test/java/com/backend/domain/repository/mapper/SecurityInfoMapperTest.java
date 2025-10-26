package com.backend.domain.repository.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.TreeResponse;
import com.backend.domain.repository.service.mapper.SecurityInfoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SecurityInfoMapperTest {

    private final SecurityInfoMapper mapper = new SecurityInfoMapper();

    @Test
    @DisplayName("TreeResponse가 null이면 기본값으로 처리")
    void mapNullTree_shouldHandleGracefully() {
        RepositoryData data = new RepositoryData();
        mapper.mapSecurityInfo(data, null);
        assertThat(data.isHasSensitiveFile()).isFalse();
    }

    @Test
    @DisplayName(".env는 민감 파일이지만 .env.example은 안전 파일로 처리")
    void mapEnvFiles_shouldDistinguishRealAndExample() {
        RepositoryData data = new RepositoryData();
        TreeResponse tree = new TreeResponse(
                List.of(
                        new TreeResponse.TreeItem(".env", "blob"),
                        new TreeResponse.TreeItem(".env.example", "blob"),
                        new TreeResponse.TreeItem(".env.local", "blob")
                ),
                false
        );
        mapper.mapSecurityInfo(data, tree);
        assertThat(data.getSensitiveFilePaths())
                .contains(".env", ".env.local")
                .doesNotContain(".env.example");
    }

    @Test
    @DisplayName("secret.json은 탐지하지만 secret.json.example은 탐지하지 않아야 함")
    void securityMapper_shouldDistinguishSensitiveAndSafeFiles() {
        RepositoryData data = new RepositoryData();
        TreeResponse tree = new TreeResponse(
                List.of(
                        new TreeResponse.TreeItem("config/secret.json", "blob"),
                        new TreeResponse.TreeItem("config/secret.json.example", "blob")
                ),
                false
        );
        mapper.mapSecurityInfo(data, tree);
        assertThat(data.isHasSensitiveFile()).isTrue();
        assertThat(data.getSensitiveFilePaths()).contains("config/secret.json");
    }

    @Test
    @DisplayName("다양한 빌드 파일들이 올바르게 감지되어야 함")
    void mapVariousBuildFiles_shouldBeDetected() {
        RepositoryData data = new RepositoryData();
        TreeResponse tree = new TreeResponse(
                List.of(
                        new TreeResponse.TreeItem("pom.xml", "blob"),
                        new TreeResponse.TreeItem("build.gradle", "blob"),
                        new TreeResponse.TreeItem("package.json", "blob"),
                        new TreeResponse.TreeItem("requirements.txt", "blob")
                ),
                false
        );
        mapper.mapSecurityInfo(data, tree);
        assertThat(data.isHasBuildFile()).isTrue();
        assertThat(data.getBuildFiles()).hasSize(4);
    }
}
