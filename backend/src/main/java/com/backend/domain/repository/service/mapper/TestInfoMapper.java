package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.TreeResponse;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class TestInfoMapper {
    // ResponseData 테스트 구성 [test 파일 여부 관련]
    private static final List<String> TEST_DIRECTORY_PATTERNS = List.of(
            "^src/test/.*",
            "^test/.*",
            "^tests/.*",
            ".*/__tests__/.*",
            ".*/test/.*",
            ".*/tests/.*",
            ".*/spec/.*"
    );

    // 테스트 파일 확장자 패턴
    private static final List<String> TEST_FILE_PATTERNS = List.of(
            ".*Test\\.(java|kt|js|ts|py|rb|go|rs)$",
            ".*Tests\\.(java|kt|js|ts|py|rb|go|rs)$", ".*TestCase\\.(java|kt|js|ts|py|rb|go|rs)$", ".*\\.test\\.(js|ts|jsx|tsx)$",
            ".*\\.spec\\.(js|ts|jsx|tsx)$", ".*_test\\.(py|go|rs|rb)$", "^test_.*\\.py$",
            ".*_spec\\.rb$", ".*IT\\.(java|kt)$", ".*[Tt]est.*\\.(c|cpp|cc|cxx)$",
            ".*Integration.*Test\\.(java|kt)$", ".*End.*To.*End.*Test\\.(java|kt)$", ".*E2E.*Test\\.(java|kt)$",
            ".*Unit.*Test\\.(java|kt)$", ".*Functional.*Test\\.(java|kt)$", ".*Contract.*Test\\.(java|kt)$",
            ".*Performance.*Test\\.(java|kt)$", ".*Load.*Test\\.(java|kt)$", ".*Smoke.*Test\\.(java|kt)$",
            ".*Acceptance.*Test\\.(java|kt)$", ".*TestBase\\.(java|kt)$", ".*TestUtils?\\.(java|kt)$",
            ".*TestHelper\\.(java|kt)$", ".*TestData\\.(java|kt)$", ".*TestConfig\\.(java|kt)$",
            ".*TestSuite\\.(java|kt)$", ".*ApplicationTests\\.(java|kt)$", ".*ContextTest\\.(java|kt)$",
            ".*SliceTest\\.(java|kt)$", ".*\\.e2e-spec\\.(js|ts)$", ".*\\.integration\\.(js|ts)$",
            ".*\\.unit\\.(js|ts)$", "^test.*\\.py$", ".*test.*\\.py$",
            ".*_integration_test\\.go$", ".*_unit_test\\.go$", ".*_integration_test\\.rs$",
            ".*/tests\\.rs$", ".*_feature_spec\\.rb$", ".*_integration_spec\\.rb$", ".*Test\\.(php)$", ".*_test\\.(php)$",
            ".*Spec\\.(java|kt)$", ".*Specification\\.(java|kt)$", ".*Scenario\\.(java|kt|py|ts)$",
            ".*Feature\\.(java|kt|py|rb)$", ".*Example\\.(java|kt|py)$", ".*Benchmark\\.(go|rs|py)$",
            ".*MockTest\\.(java|kt|ts|py)$", ".*ValidatorTest\\.(java|kt|ts|py)$", ".*ControllerTest\\.(java|kt|ts)$",
            ".*ServiceTest\\.(java|kt|ts)$", ".*RepositoryTest\\.(java|kt)$", ".*APITest\\.(java|kt|ts|py)$",
            ".*UITest\\.(java|kt|ts)$", ".*BrowserTest\\.(js|ts|py)$", ".*Acceptance\\.(java|kt|py)$", ".*RegressionTest\\.(java|kt|py)$"
    );

    // 소스 파일 확장자
    private static final List<String> SOURCE_FILE_EXTENSIONS = List.of(
            ".java", ".kt", ".js", ".ts", ".jsx", ".tsx", ".py", ".rb", ".go", ".rs", ".cpp", ".c", ".cs", ".php", ".swift"
    );

    public void mapTestInfo(RepositoryData data, TreeResponse response) {
        if (response == null || response.tree().isEmpty()) {
            setEmptyTestData(data);
            return;
        }

        List<String> allPaths = extractAllPaths(response);
        List<String> filePaths = extractFilePaths(response);

        // 1. 테스트 디렉터리 존재 여부
        boolean hasTestDirectory = checkTestDirectoryExists(allPaths);
        data.setHasTestDirectory(hasTestDirectory);

        // 2. 테스트 파일 개수
        int testFileCount = countTestFiles(filePaths);
        data.setTestFileCount(testFileCount);

        // 3. 소스 파일 개수
        int sourceFileCount = countSourceFiles(filePaths);
        data.setSourceFileCount(sourceFileCount);

        // 4. 테스트 커버리지 비율 계산
        double testCoverageRatio = calculateTestCoverageRatio(testFileCount, sourceFileCount);
        data.setTestCoverageRatio(testCoverageRatio);
    }

    private void setEmptyTestData(RepositoryData data) {
        data.setHasTestDirectory(false);
        data.setTestFileCount(0);
        data.setSourceFileCount(0);
        data.setTestCoverageRatio(0.0);
    }

    private List<String> extractAllPaths(TreeResponse response) {
        if (response == null || response.tree() == null) {
            return Collections.emptyList();
        }

        return response.tree().stream()
                .map(TreeResponse.TreeItem::path)
                .collect(Collectors.toList());
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

    private boolean checkTestDirectoryExists(List<String> allPaths) {
        return TEST_DIRECTORY_PATTERNS.stream()
                .anyMatch(pattern -> allPaths.stream().anyMatch(path ->
                        path.matches(pattern)));
    }

    private int countTestFiles(List<String> filePaths) {
        return (int) filePaths.stream()
                .filter(this::isTestFile)
                .count();
    }

    private boolean isTestFile(String filePath) {
        boolean inTestDirectory = TEST_DIRECTORY_PATTERNS.stream()
                .anyMatch(pattern -> filePath.matches(pattern));

        boolean matchesTestPattern = TEST_FILE_PATTERNS.stream()
                .anyMatch(pattern -> filePath.matches(pattern));

        return inTestDirectory || matchesTestPattern;
    }

    private String extractFileName(String filePath) {
        if(filePath == null || filePath.isEmpty()) {
            return "";
        }

        int lastSlashIndex = filePath.lastIndexOf('/');
        return lastSlashIndex >= 0? filePath.substring(lastSlashIndex + 1) : filePath;
    }

    private int countSourceFiles(List<String> filePaths) {
        return (int) filePaths.stream()
                .filter(this::isSourceFile)
                .filter(path -> !isTestFile(path))
                .count();
    }

    private boolean isSourceFile(String filePath) {
        return SOURCE_FILE_EXTENSIONS.stream()
                .anyMatch(filePath::endsWith);
    }

    private double calculateTestCoverageRatio(int testFileCount, int sourceFileCount) {
        if(sourceFileCount == 0) {
            return 0.0;
        }

        double ratio = (double) testFileCount / sourceFileCount;
        return Math.round(ratio * 1000.0) / 1000.0;
    }
}
