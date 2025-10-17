package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.TreeResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CicdInfoMapper {
    // ResponseData CI/CD 관련 [CI/CD 존재 여부 관련]
    private static final List<String> CICD_FILE_PATTERNS = List.of(
            ".*\\.github/workflows/.*\\.ya?ml$",
            ".*/Jenkinsfile$",
            ".*\\.gitlab-ci\\.ya?ml$",
            ".*\\.circleci/config\\.ya?ml$",
            ".*\\.travis\\.ya?ml$",
            ".*/azure-pipelines\\.ya?ml$"
    );

    private static final List<String> BUILD_FILE_PATTERNS = List.of(
            ".*/pom\\.xml$",
            ".*/build\\.gradle(\\.kts)?$",
            ".*/package\\.json$",
            ".*/Cargo\\.toml$",
            ".*/go\\.mod$",
            ".*/requirements\\.txt$",
            ".*/setup\\.py$",
            ".*/Dockerfile$"
    );

    public void mapCicdInfo(RepositoryData data, TreeResponse response) {
        if (response == null || response.tree().isEmpty()) {
            setEmptyCicdData(data);
            return;
        }

        List<String> filePaths = extractFilePaths(response);

        // CI/CD 설정 확인
        boolean hasCICD = checkCICDConfiguration(filePaths);
        List<String> cicdFiles = getCICDFiles(filePaths);
        data.setHasCICD(hasCICD);
        data.setCicdFiles(cicdFiles);

        // 빌드 스크립트 확인
        boolean hasBuildFile = checkBuildConfiguration(filePaths);
        List<String> buildFiles = getBuildFiles(filePaths);
        data.setHasBuildFile(hasBuildFile);
        data.setBuildFiles(buildFiles);

        // Dockerfile 별도 확인
        boolean hasDockerfile = filePaths.stream()
                .anyMatch(path -> path.matches(".*/Dockerfile$"));
        data.setHasDockerfile(hasDockerfile);
    }

    private void setEmptyCicdData(RepositoryData data) {
        data.setHasCICD(false);
        data.setCicdFiles(Collections.emptyList());
        data.setHasDockerfile(false);
    }

    private List<String> extractFilePaths(TreeResponse response) {
        if (response == null || response.tree() == null) {
            return Collections.emptyList();
        }

        return response.tree().stream()
                .filter(item -> "blob".equals(item.type()))
                .map(TreeResponse.TreeItem::path)
                .collect(Collectors.toList());
    }

    private boolean checkCICDConfiguration(List<String> filePaths) {
        return CICD_FILE_PATTERNS.stream()
                .anyMatch(pattern -> filePaths.stream().anyMatch(path ->
                        path.matches(pattern)));
    }

    private boolean checkBuildConfiguration(List<String> filePaths) {
        boolean hasRequirementsTxt = filePaths.stream()
                .anyMatch(path -> path.matches(".*/requirements\\.txt$"));
        boolean hasSetupPy = filePaths.stream()
                .anyMatch(path -> path.matches(".*/setup\\.py$"));
        boolean hasPythonBuild = hasRequirementsTxt && hasSetupPy;

        boolean hasOtherBuildFile = BUILD_FILE_PATTERNS.stream()
                .filter(pattern -> !pattern.contains("requirements") && !pattern.contains("setup"))
                .anyMatch(pattern -> filePaths.stream().anyMatch(path ->
                        path.matches(pattern)));

        return hasOtherBuildFile || hasPythonBuild;
    }

    private List<String> getCICDFiles(List<String> filePaths) {
        return filePaths.stream()
                .filter(path -> CICD_FILE_PATTERNS.stream().anyMatch(path::matches))
                .collect(Collectors.toList());
    }

    private List<String> getBuildFiles(List<String> filePaths) {
        return filePaths.stream()
                .filter(path -> BUILD_FILE_PATTERNS.stream().anyMatch(path::matches))
                .collect(Collectors.toList());
    }
}
