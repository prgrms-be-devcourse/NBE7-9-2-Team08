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

    // 민감 파일 패턴 정의
    private static final List<String> SENSITIVE_PATTERNS = List.of(
            "\\.env$",                    // .env 파일
            "\\.(pem|key|p12|pfx)$",     // 인증서/키 파일
            "id_rsa$",                   // SSH 개인키
            "authorized_keys$",          // SSH 인증키
            "credentials\\."             // credentials.* 파일
    );

    // 민감 파일 예외 패턴
    private static final List<String> SAFE_PATTERNS = List.of(
            "\\.env\\.(example|template|sample)$",
            "credentials\\.(example|sample|template)$"
    );

    // 빌드 파일 정의
    private static final List<String> BUILD_FILES = List.of(
            "pom.xml",
            "build.gradle",
            "build.gradle.kts",
            "package.json",
            "Cargo.toml",
            "go.mod",
            "requirements.txt",
            "setup.py",
            "Dockerfile"
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
        String fileName = extractFileName(filePath);

        if (isSafeFile(fileName)) {
            return false;
        }

        return SENSITIVE_PATTERNS.stream()
                .anyMatch(pattern -> fileName.matches(pattern));
    }

    private boolean isSafeFile(String fileName) {
        return SAFE_PATTERNS.stream()
                .anyMatch(pattern -> fileName.matches(pattern));
    }

    private List<String> findBuildFiles(List<String> filePaths) {
        return filePaths.stream()
                .filter(this::isBuildFile)
                .collect(Collectors.toList());
    }

    private boolean isBuildFile(String filePath) {
        String fileName = extractFileName(filePath);
        return BUILD_FILES.contains(fileName);
    }

    private String extractFileName(String filePath) {
        if (filePath == null || filePath.isEmpty()) {
            return "";
        }

        int lastSlashIndex = filePath.lastIndexOf('/');
        return lastSlashIndex >= 0 ? filePath.substring(lastSlashIndex + 1) : filePath;
    }
}
