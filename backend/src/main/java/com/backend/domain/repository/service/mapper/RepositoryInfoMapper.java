package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.RepoResponse;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.ZoneId;

@Component
public class RepositoryInfoMapper {
    // ResponseData 메타 정보 [기본 정보]
    public void mapBasicInfo(RepositoryData data, RepoResponse response) {
        // Repository 이름
        data.setRepositoryName(response.fullName());
        // Repository URL
        data.setRepositoryUrl(response.htmlUrl());
        // Repository 설명
        data.setDescription(response.description());
        // Repository 주요 사용 언어
        data.setPrimaryLanguage(response.language());
        // Repository 생성 날짜
        ZoneId kst = ZoneId.of("Asia/Seoul");
        LocalDateTime createdAtKST = response.createdAt()
                .atZoneSameInstant(kst)
                .toLocalDateTime();
        data.setRepositoryCreatedAt(createdAtKST);
    }
}
