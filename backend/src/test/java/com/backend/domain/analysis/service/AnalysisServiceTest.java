package com.backend.domain.analysis.service;

import com.backend.domain.evaluation.service.EvaluationService;
import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.service.RepositoryService;
import com.backend.global.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.BDDMockito.times;

@SpringBootTest
@ActiveProfiles("test")
class AnalysisServiceTest {

    @Autowired
    private AnalysisService analysisService;

    @MockitoBean
    private RepositoryService repositoryService;   // 깃허브 수집 Mock

    @MockitoBean
    private EvaluationService evaluationService;   // AI 평가 Mock

    @Test
    @DisplayName("analyze → 수집 후 evaluateAndSave 한 번 호출")
    void analyze_callsEvaluateOnce() {
        // given
        String url = "https://github.com/owner/repo";
        RepositoryData fake = new RepositoryData();
        // RepositoryData가 세터가 없을 수도 있으니, 인자 값 검증은 캡처만 사용
        given(repositoryService.fetchAndSaveRepository("owner", "repo")).willReturn(fake);

        // when
        analysisService.analyze(url);

        // then
        ArgumentCaptor<RepositoryData> captor = ArgumentCaptor.forClass(RepositoryData.class);
        then(evaluationService).should(times(1)).evaluateAndSave(captor.capture());
        assertThat(captor.getValue()).isNotNull();
        then(repositoryService).should().fetchAndSaveRepository("owner", "repo");
    }

    @Test
    @DisplayName("analyze → 잘못된 URL이면 evaluateAndSave 호출 안 함")
    void analyze_invalidUrl_doesNotCallEvaluate() {
        assertThatThrownBy(() -> analysisService.analyze("https://notgithub.com/owner/repo"))
                .isInstanceOf(BusinessException.class);

        then(repositoryService).shouldHaveNoInteractions();
        then(evaluationService).shouldHaveNoInteractions();
    }
}
