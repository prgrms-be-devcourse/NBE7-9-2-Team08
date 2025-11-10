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
// 테스트 디렉토리
    private static final List<String> TEST_DIRECTORY_PATTERNS = List.of(
            "^src/test/.*",
            ".*/(test|tests|spec|specs|__tests__)/.*",
            ".*/(integration-tests?|functional-tests?|acceptance-tests?|e2e|qa|itest|utest)/.*"
    );

    // 테스트 파일
    private static final List<String> TEST_FILE_PATTERNS = List.of(
            // Java/Kotlin
            ".*(Test|Tests|TestCase|IT|Spec|Feature|Scenario)\\.(java|kt)$",
            ".*(Integration|Application|Unit|Functional|E2E|Performance|Load|Smoke|Acceptance|Regression|UITest).*Test\\.(java|kt)$",
            ".*(TestBase|TestUtils?|TestHelper|TestData|TestConfig|TestSuite)\\.(java|kt)$",

            // JavaScript/TypeScript
            ".*\\.(test|spec|e2e-spec|integration|unit|browser)\\.(js|ts|jsx|tsx)$",

            // Python
            "^test_.*\\.py$",
            ".*_test\\.py$",

            // Go
            ".*_test\\.go$",
            ".*_(integration|unit)_test\\.go$",

            // Ruby
            ".*_spec\\.rb$",

            // Rust
            ".*_test\\.rs$",
            ".*/tests\\.rs$",

            // C/C++
            ".*[Tt]est.*\\.(c|cpp|cc|cxx|h|hpp)$",

            // Dart
            ".*_test\\.dart$",

            // PHP / Swift / C#
            ".*Test\\.(php|swift|cs)$",

            // 기타 패턴
            ".*(Mock|Validator|Controller|Service|Repository|API|UI)Test\\.(java|kt|ts|py|cs|php|swift)$"
    );

    // 소스 파일 확장자
    private static final List<String> SOURCE_FILE_EXTENSIONS = List.of(
            ".java", ".kt", ".scala", ".js", ".ts", ".jsx", ".tsx",
            ".py", ".rb", ".go", ".rs", ".cpp", ".c", ".cs",
            ".php", ".swift", ".m", ".mm", ".dart", ".h", ".hpp"
    );

    public void mapTestInfo(RepositoryData data, TreeResponse response) {
        if (response == null || response.tree().isEmpty() || response.tree() == null) {
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
