package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
public class ReadmeInfoMapper {

    public void mapReadmeInfo(RepositoryData data, String readmeContent) {  // String으로 변경
        if (readmeContent == null || readmeContent.trim().isEmpty()) {
            setEmptyReadmeData(data);
            return;
        }

        data.setHasReadme(true);
        data.setReadmeContent(readmeContent);
        data.setReadmeLength(readmeContent.length());

        List<String> sectionTitles = extractSectionTitles(readmeContent);
        data.setReadmeSectionCount(sectionTitles.size());
        data.setReadmeSectionTitles(sectionTitles);
    }

    private void setEmptyReadmeData(RepositoryData data) {
        data.setHasReadme(false);
        data.setReadmeLength(0);
        data.setReadmeSectionCount(0);
        data.setReadmeSectionTitles(List.of());
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
