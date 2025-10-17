package com.backend.domain.repository.service.mapper;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.TreeResponse;
import org.springframework.stereotype.Component;

@Component
public class TestInfoMapper {
    // ResponseData 테스트 구성 [test 파일 여부 관련]
    public void mapTestInfo(RepositoryData data, TreeResponse response) {
        if (response == null || response.tree().isEmpty()) {
            setEmptyTestData(data);
            return;
        }
    }

    private void setEmptyTestData(RepositoryData data) {
        data.setHasTestDirectory(false);
        data.setTestFileCount(0);
        data.setSourceFileCount(0);
        data.setTestCoverageRatio(0.0);
    }
}
