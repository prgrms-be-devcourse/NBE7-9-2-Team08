package com.backend.domain.repository.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.service.mapper.ReadmeInfoMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ReadmeInfoMapperTest {

    private final ReadmeInfoMapper mapper = new ReadmeInfoMapper();

    @Test
    @DisplayName("README가 null일 때 기본값 설정")
    void mapNullReadme_shouldSetDefaultValues() {
        RepositoryData data = new RepositoryData();
        mapper.mapReadmeInfo(data, null);
        assertThat(data.isHasReadme()).isFalse();
        assertThat(data.getReadmeContent()).isEmpty();
        assertThat(data.getReadmeSectionCount()).isZero();
    }

    @Test
    @DisplayName("README 내용이 공백만 있을 때 hasReadme=false 여야 함")
    void mapWhitespaceOnlyReadme_shouldBeHandled() {
        RepositoryData data = new RepositoryData();
        mapper.mapReadmeInfo(data, "   \n  \n");
        assertThat(data.isHasReadme()).isFalse();
        assertThat(data.getReadmeLength()).isZero();
    }

    @Test
    @DisplayName("README 내 코드 블록 안의 #은 섹션으로 인식하지 않아야 함")
    void mapReadmeWithCodeBlock_shouldIgnoreHeadersInCode() {
        RepositoryData data = new RepositoryData();
        String readme = """
                # Main Title

                ```bash
                # not a header
                echo "hello"
                ```

                ## Section 2
                """;
        mapper.mapReadmeInfo(data, readme);
        assertThat(data.isHasReadme()).isTrue();
        assertThat(data.getReadmeSectionTitles()).containsExactly("Main Title", "Section 2");
        assertThat(data.getReadmeSectionCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("README가 HTML 헤더를 포함하더라도 Markdown 헤더만 인식해야 함")
    void readmeWithHtmlHeaders_shouldIgnoreThem() {
        RepositoryData data = new RepositoryData();
        String htmlReadme = """
                <h1>Main Title</h1>
                # Markdown Header
                <h2>Sub Title</h2>
                ## Markdown Sub
                """;
        mapper.mapReadmeInfo(data, htmlReadme);
        assertThat(data.getReadmeSectionTitles())
                .containsExactly("Markdown Header", "Markdown Sub");
    }
}
