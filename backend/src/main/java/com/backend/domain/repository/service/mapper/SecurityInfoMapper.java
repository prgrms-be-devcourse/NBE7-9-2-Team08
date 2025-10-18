package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.TreeResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class SecurityInfoMapper {
    // ResponseData 보안 [민감 파일, 빌드 파일 여부]
    private static final List<String> SENSITIVE_FILE_PATTERNS = List.of(
            ".*\\.env$", ".*\\.(pem|key|p12|pfx|crt|cer)$", ".*/id_rsa$|^id_rsa$", ".*/id_dsa$|^id_dsa$",
            ".*/authorized_keys$|^authorized_keys$", ".*credentials\\.(json|xml|yml|yaml|properties)$", ".*secret.*\\.(json|xml|yml|yaml|properties)$",
            ".*\\.(keystore|jks|p12)$", ".*/\\.aws/credentials$", ".*/\\.ssh/.*$", ".*\\.env\\..*$",
            ".*\\.aws/.*credentials.*", ".*\\.gcp/.*(key|credential).*", ".*service-account.*\\.json$", ".*firebase.*\\.json$",
            ".*google.*credentials.*\\.json$", ".*config\\.json$", ".*application(-secret|-prod)?\\.ya?ml$", ".*token.*(\\.txt|\\.json|\\.yml|\\.yaml)$",
            ".*apikey.*(\\.txt|\\.json|\\.env|\\.yml)$", ".*password.*(\\.txt|\\.json|\\.env|\\.yml)$", ".*oauth.*(\\.json|\\.yml|\\.yaml)$",
            ".*client_secret.*(\\.json|\\.yml|\\.yaml)$", ".*private.*(\\.json|\\.pem|\\.key)$", ".*jwt.*(\\.json|\\.pem|\\.key)$",
            ".*vault.*", ".*id_ecdsa$", ".*pgpass$", ".*\\.npmrc$", ".*\\.netrc$", ".*\\.bash_history$", ".*\\.zsh_history$",
            ".*\\.docker/config\\.json$", ".*terraform.*\\.tfstate.*", ".*secrets?\\.json$", ".*\\.p8$", ".*\\.bak$", ".*\\.old$", ".*\\.swp$", ".*\\.DS_Store$"
    );

    private static final List<String> SAFE_FILE_PATTERNS = List.of(
            ".*\\.env\\.(example|template|sample|dist)$", ".*credentials\\.(example|sample|template|dist)$",
            ".*secret.*\\.(example|sample|template|dist)$", ".*\\.env\\.(example|sample|template|dist|local|dev|prod|staging)$",
            ".*example.*credentials.*", ".*dummy.*(json|yaml|yml|env|properties)$",
            ".*mock.*(json|yaml|yml|env|properties)$", ".*test.*(json|yaml|yml|env|properties)$",
            ".*\\.example$", ".*\\.sample$", ".*\\.template$", ".*\\.default$", ".*/fixtures/.*", ".*/samples?/.*"

    );

    private static final List<String> BUILD_FILE_NAMES = List.of(
            "pom.xml", "build.gradle", "build.gradle.kts", "package.json", "Cargo.toml",
            "go.mod", "requirements.txt", "setup.py", "CMakeLists.txt", "Makefile", "Dockerfile",
            "gradlew", "gradlew.bat", "mvnw", "mvnw.cmd", "Gemfile", "Gemfile.lock", "composer.json", "composer.lock",
            "yarn.lock", "pnpm-lock.yaml", "package-lock.json", "mix.exs", "rebar.config",
            "build.sbt", "build.xml", "setup.cfg", "pyproject.toml", "environment.yml", "Procfile", "runtime.txt",
            "Vagrantfile", "Docker-compose.yml", "docker-compose.yml", "Chart.yaml", "values.yaml", "Makefile.am",
            "Brewfile", "Podfile", "Podfile.lock", "Fastfile", "Taskfile.yml", "taskfile.yml", "Justfile"

    );

    public void mapSecurityInfo(RepositoryData data, TreeResponse response) {
        if (response == null || response.tree().isEmpty()) {
            setEmptySecurityData(data);
            return;
        }
        // 디렉터리 제외하고, 파일만 추출
        List<String> allFilePaths = extractFilePaths(response);

        // 민감 파일 체크
        List<String> sensitiveFiles = findSensitiveFiles(allFilePaths);
        data.setHasSensitiveFile(!sensitiveFiles.isEmpty());
        data.setSensitiveFilePaths(sensitiveFiles);

        // 빌드 파일 체크
        List<String> buildFiles = findBuildFiles(allFilePaths);
        data.setHasBuildFile(!buildFiles.isEmpty());
        data.setBuildFiles(buildFiles);
    }

    private void setEmptySecurityData(RepositoryData data) {
        data.setHasSensitiveFile(false);
        data.setSensitiveFilePaths(Collections.emptyList());
        data.setHasBuildFile(false);
        data.setBuildFiles(Collections.emptyList());
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

    private List<String> findSensitiveFiles(List<String> filePaths) {
        return filePaths.stream()
                .filter(this::isSensitiveFile)
                .collect(Collectors.toList());
    }

    private boolean isSensitiveFile(String filePath) {
        if (isSafeFile(filePath)) {
            return false;
        }

        return SENSITIVE_FILE_PATTERNS.stream()
                .anyMatch(filePath::matches);
    }

    private boolean isSafeFile(String filePath) {
        return SAFE_FILE_PATTERNS.stream()
                .anyMatch(filePath::matches);
    }

    private List<String> findBuildFiles(List<String> filePaths) {
        return filePaths.stream()
                .filter(this::isBuildFile)
                .collect(Collectors.toList());
    }

    private boolean isBuildFile(String filePath) {
        String fileName = extractFileName(filePath);
        return BUILD_FILE_NAMES.contains(fileName);
    }

    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }

        int lastSlashIndex = filePath.lastIndexOf('/');
        return lastSlashIndex >= 0 ? filePath.substring(lastSlashIndex + 1) : filePath;
    }
}
