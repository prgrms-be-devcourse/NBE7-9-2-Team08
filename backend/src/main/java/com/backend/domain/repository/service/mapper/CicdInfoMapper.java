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
    private static final String GITHUB_WORKFLOWS_PATTERN = "^\\.github/workflows/.*\\.(yml|yaml)$";

    private static final List<String> CICD_FILE_PATTERNS = List.of(
            "^\\.gitlab-ci\\.(yml|yaml)$", "^\\.travis\\.(yml|yaml)$", "^azure-pipelines\\.(yml|yaml)$",
            "^circle\\.(yml|yaml)$", "^\\.circleci/config\\.(yml|yaml)$", "^\\.drone\\.(yml|yaml)$",
            "^\\.bitbucket-pipelines\\.(yml|yaml)$", "^\\.buildkite\\/(pipeline\\.)?(yml|yaml)$", "^\\.teamcity\\.(settings|config)\\.(xml|kts|yaml)$",
            "^\\.appveyor\\.(yml|yaml)$", "^\\.github/actions/.*\\.(yml|yaml)$", "^\\.buddy/.*\\.(yml|yaml)$", "^\\.codefresh/.*\\.(yml|yaml)$",
            "^\\.bitrise\\.yml$", "^\\.wercker/.*\\.(yml|yaml)$", "^\\.buildkite/pipeline\\.(yml|yaml)$", "^\\.concourse/.*\\.(yml|yaml)$",
            "^\\.semaphore/.*\\.(yml|yaml)$", "^\\.harness/.*\\.(yml|yaml)$"
    );

    private static final List<String> JENKINSFILE_PATTERNS = List.of(".*/Jenkinsfile$|^Jenkinsfile$", ".*/Jenkinsfile(\\..*)?$|^Jenkinsfile(\\..*)?$");

    private static final List<String> BUILD_FILE_PATTERNS = List.of(
            "^pom\\.xml$|.*/pom\\.xml$",
            "^build\\.gradle(\\.kts)?$|.*/build\\.gradle(\\.kts)?$", "^package\\.json$|.*/package\\.json$",
            "^Cargo\\.toml$|.*/Cargo\\.toml$", "^go\\.mod$|.*/go\\.mod$", "^requirements\\.txt$|.*/requirements\\.txt$",
            "^setup\\.py$|.*/setup\\.py$", "^CMakeLists\\.txt$|.*/CMakeLists\\.txt$", "^Makefile$|.*/Makefile$",
            "^gradlew(\\.bat)?$|.*/gradlew(\\.bat)?$", "^mvnw(\\.cmd)?$|.*/mvnw(\\.cmd)?$", "^Gemfile(\\.lock)?$|.*/Gemfile(\\.lock)?$",
            "^composer\\.(json|lock)$|.*/composer\\.(json|lock)$", "^yarn\\.lock$|.*/yarn\\.lock$", "^pnpm-lock\\.yaml$|.*/pnpm-lock\\.yaml$",
            "^package-lock\\.json$|.*/package-lock\\.json$", "^pyproject\\.toml$|.*/pyproject\\.toml$", "^setup\\.cfg$|.*/setup\\.cfg$",
            "^environment\\.yml$|.*/environment\\.yml$", "^mix\\.exs$|.*/mix\\.exs$", "^build\\.sbt$|.*/build\\.sbt$",
            "^Vagrantfile$|.*/Vagrantfile$", "^Chart\\.yaml$|.*/Chart\\.yaml$", "^values\\.yaml$|.*/values\\.yaml$",
            "^Taskfile\\.ya?ml$|.*/Taskfile\\.ya?ml$", "^Justfile$|.*/Justfile$", "^Brewfile$|.*/Brewfile$",
            "^Podfile(\\.lock)?$|.*/Podfile(\\.lock)?$", "^Fastfile$|.*/Fastfile$", "^Makefile\\.am$|.*/Makefile\\.am$", "^CMakeCache\\.txt$|.*/CMakeCache\\.txt$"
    );

    private static final List<String> DOCKERFILE_PATTERNS = List.of(
            "^Dockerfile.*$|.*/Dockerfile.*$",
            "^Dockerfile.*$|.*/Dockerfile.*$|.*\\.dockerfile$",
            "^docker-compose\\.ya?ml$|.*/docker-compose\\.ya?ml$",
            "^compose\\.ya?ml$|.*/compose\\.ya?ml$"
    );

    public void mapCicdInfo(RepositoryData data, TreeResponse response) {
        if (response == null || response.tree().isEmpty()) {
            setEmptyCicdData(data);
            return;
        }

        List<String> filePaths = extractFilePaths(response);

        // CI/CD 설정 확인
        List<String> cicdFiles = findCicdFiles(filePaths);
        data.setHasCICD(!cicdFiles.isEmpty());
        data.setCicdFiles(cicdFiles);

        // 빌드 스크립트 확인
        List<String> buildFiles = findBuildFiles(filePaths);
        data.setHasBuildFile(!buildFiles.isEmpty());
        data.setBuildFiles(buildFiles);

        // Dockerfile 별도 확인
        boolean hasDockerfile = hasDockerFiles(filePaths);
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

    private List<String> findCicdFiles(List<String> filePaths) {
        return filePaths.stream()
                .filter(this::isCicdFile)
                .collect(Collectors.toList());
    }

    private boolean isCicdFile(String filePath) {
        if (filePath.matches(GITHUB_WORKFLOWS_PATTERN)) {
            return true;
        }

        if (JENKINSFILE_PATTERNS.stream().anyMatch(filePath::matches)) {
            return true;
        }

        return CICD_FILE_PATTERNS.stream()
                .anyMatch(filePath::matches);
    }

    private List<String> findBuildFiles(List<String> filePaths) {
        return filePaths.stream()
                .filter(this::isBuildFile)
                .collect(Collectors.toList());
    }

    private boolean isBuildFile(String filePath) {
        return BUILD_FILE_PATTERNS.stream()
                .anyMatch(filePath::matches);
    }

    private boolean hasDockerFiles(List<String> filePaths) {
        return filePaths.stream()
                .anyMatch(path -> DOCKERFILE_PATTERNS.stream()
                        .anyMatch(path::matches));
    }
}
