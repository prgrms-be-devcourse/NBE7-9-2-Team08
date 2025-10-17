package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ReadmeInfoMapper {
    // ResponseData 문서화 품질 [README 관련]
    public void mapReadmeInfo(RepositoryData data, String readmeContent) {  // String으로 변경
        if (readmeContent == null || readmeContent.trim().isEmpty()) {
            setEmptyReadmeData(data);
            return;
        }

        // README.md 존재 여부
        data.setHasReadme(true);
        // README.md 전체 내용
        data.setReadmeContent(readmeContent);
        // README.md 글자 수
        data.setReadmeLength(readmeContent.length());


        List<String> sectionTitles = extractSectionTitles(readmeContent);
        // README.md # 개수
        data.setReadmeSectionCount(sectionTitles.size());
        // README.md 섹션 제목 리스트
        data.setReadmeSectionTitles(sectionTitles);
    }

    private void setEmptyReadmeData(RepositoryData data) {
        data.setHasReadme(false);
        data.setReadmeLength(0);
        data.setReadmeSectionCount(0);
        data.setReadmeSectionTitles(Collections.emptyList());
        data.setReadmeContent("");
    }

    private List<String> extractSectionTitles(String content) {
        Pattern headerPattern = Pattern.compile("^(#{1,6})\\s+(.+)$");
        boolean inCodeBlock = false;
        List<String> titles = new ArrayList<>();

        for (String line : content.split("\n")) {
            String trimmed = line.trim();

            if(trimmed.startsWith("```")) {
                inCodeBlock = !inCodeBlock;
                continue;
            }

            if (!inCodeBlock) {
                Matcher matcher = headerPattern.matcher(trimmed);
                if (matcher.matches()) {
                    titles.add(matcher.group(2).trim());
                }
            }
        }

        return titles;
    }
}
