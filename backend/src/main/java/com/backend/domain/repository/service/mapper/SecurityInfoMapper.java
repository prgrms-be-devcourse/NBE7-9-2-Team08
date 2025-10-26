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
            // 환경 변수 파일
            ".*\\.env$",
            ".*\\.env\\.prod(uction)?$",
            ".*\\.env\\.local$",

            // 인증서 및 키
            ".*\\.(pem|key|p12|pfx|crt|cer|p8)$",
            ".*/id_rsa$|^id_rsa$",
            ".*/id_dsa$|^id_dsa$",
            ".*/id_ecdsa$|^id_ecdsa$",
            ".*/authorized_keys$|^authorized_keys$",
            ".*\\.(keystore|jks)$",

            // 민감한 설정
            ".*application-secret\\.ya?ml$",
            ".*application-prod\\.ya?ml$",
            ".*credentials\\.(json|xml|yml|yaml|properties)$",
            ".*secret.*\\.(json|xml|yml|yaml|properties)$",
            ".*secrets?\\.json$",

            // 클라우드 인증
            ".*/\\.aws/credentials$",
            ".*service-account.*\\.json$",
            ".*firebase.*\\.json$",
            ".*google.*credentials.*\\.json$",

            // 토큰 / API 키
            ".*token.*\\.txt$",
            ".*apikey.*\\.txt$",
            ".*password.*\\.txt$",
            ".*client_secret.*\\.(json|yml|yaml)$",
            ".*oauth.*\\.json$",

            // SSH 관련
            ".*/\\.ssh/id_.*$",
            ".*/\\.ssh/config$",

            // 기타 확실한 민감 파일
            ".*pgpass$",
            ".*\\.netrc$"
    );

    private static final List<String> SAFE_FILE_PATTERNS = List.of(
            // 예시/템플릿 파일들
            ".*\\.(example|template|sample|dist|default)$",
            ".*\\.env\\.(example|template|sample|dist)$",
            ".*credentials\\.(example|sample|template)$",
            ".*secret.*\\.(example|sample|template)$",

            // 테스트/더미 데이터
            ".*test.*\\.(json|yaml|yml|env|properties)$",
            ".*mock.*\\.(json|yaml|yml|env|properties)$",
            ".*dummy.*\\.(json|yaml|yml|env|properties)$",

            // 예시 디렉토리들
            ".*/fixtures/.*",
            ".*/samples?/.*",
            ".*/examples?/.*"
    );

    private static final List<String> BUILD_FILE_NAMES = List.of(
            // 주요 빌드 파일들
            "pom.xml", "build.gradle", "build.gradle.kts",
            "package.json", "package-lock.json", "yarn.lock", "pnpm-lock.yaml",
            "Cargo.toml", "go.mod", "requirements.txt", "pyproject.toml", "setup.py",
            "CMakeLists.txt", "Makefile", "Dockerfile",

            // 빌드 스크립트들
            "gradlew", "gradlew.bat", "mvnw", "mvnw.cmd",

            // 언어별 의존성 파일들
            "Gemfile", "Gemfile.lock", "composer.json", "composer.lock",
            "mix.exs", "build.sbt"
    );

    public void mapSecurityInfo(RepositoryData data, TreeResponse response) {
        if (response == null || response.tree() == null || response.tree().isEmpty()) {
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
