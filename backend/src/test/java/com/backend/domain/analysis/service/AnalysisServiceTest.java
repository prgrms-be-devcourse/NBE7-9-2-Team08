package com.backend.domain.analysis.service;

import com.backend.domain.analysis.lock.RedisLockManager;
import com.backend.domain.evaluation.service.EvaluationService;
import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.domain.repository.service.RepositoryService;
import com.backend.domain.user.service.EmailService;
import com.backend.domain.user.util.JwtUtil;
import com.backend.global.exception.BusinessException;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;

import static com.backend.domain.repository.dto.RepositoryDataFixture.createMinimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@SpringBootTest
@ActiveProfiles("test")
class AnalysisServiceTest {

    @Autowired
    private AnalysisService analysisService;

    @MockitoBean
    private RepositoryService repositoryService;

    @MockitoBean
    private EvaluationService evaluationService;

    @MockitoBean
    private SseProgressNotifier sseProgressNotifier;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private RepositoryJpaRepository repositoryJpaRepository;

    @MockitoBean
    private RedisLockManager lockManager;

    @MockitoBean
    private EmailService emailService;

    private MockHttpServletRequest createAuthenticatedRequest(Long userId) {
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setCookies(new Cookie("token", "dummy-token"));
        given(jwtUtil.getUserId(request)).willReturn(userId);
        return request;
    }

    @Test
    @DisplayName("analyze → 수집 후 evaluateAndSave 한 번 호출")
    void analyze_callsEvaluateOnce() {
        // given
        String url = "https://github.com/owner/repo";
        Long userId = 1L;
        MockHttpServletRequest request = createAuthenticatedRequest(userId);

        given(lockManager.tryLock(anyString())).willReturn(true);

        // Fixture 사용
        RepositoryData fakeData = createMinimal();
        fakeData.setRepositoryUrl("https://github.com/owner/repo");
        given(repositoryService.fetchAndSaveRepository("owner", "repo", userId))
                .willReturn(fakeData);

        // Repositories Mock
        Repositories fakeRepo = mock(Repositories.class);
        given(fakeRepo.getId()).willReturn(1L);
        given(repositoryJpaRepository.findByHtmlUrlAndUserId(anyString(), anyLong()))
                .willReturn(Optional.of(fakeRepo));

        // when
        Long repositoryId = analysisService.analyze(url, request);

        // then
        assertThat(repositoryId).isEqualTo(1L);

        ArgumentCaptor<RepositoryData> captor = ArgumentCaptor.forClass(RepositoryData.class);
        then(evaluationService).should(times(1)).evaluateAndSave(captor.capture(), eq(userId));
        assertThat(captor.getValue()).isNotNull();
        then(repositoryService).should().fetchAndSaveRepository("owner", "repo", userId);
        then(lockManager).should().releaseLock(anyString());
    }

    @Test
    @DisplayName("analyze → 잘못된 URL이면 evaluateAndSave 호출 안 함")
    void analyze_invalidUrl_doesNotCallEvaluate() {
        // given
        Long userId = 1L;
        MockHttpServletRequest request = createAuthenticatedRequest(userId);

        // when & then
        assertThatThrownBy(() -> analysisService.analyze("https://notgithub.com/owner/repo", request))
                .isInstanceOf(BusinessException.class);

        then(repositoryService).shouldHaveNoInteractions();
        then(evaluationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("analyze → 인증되지 않은 사용자는 분석 불가")
    void analyze_unauthenticated_throwsException() {
        // given
        String url = "https://github.com/owner/repo";
        MockHttpServletRequest request = new MockHttpServletRequest();
        given(jwtUtil.getUserId(request)).willReturn(null);

        // when & then
        assertThatThrownBy(() -> analysisService.analyze(url, request))
                .isInstanceOf(BusinessException.class);

        then(repositoryService).shouldHaveNoInteractions();
        then(evaluationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("analyze → 중복 분석 요청 시 락 획득 실패")
    void analyze_duplicateRequest_throwsException() {
        // given
        String url = "https://github.com/owner/repo";
        Long userId = 1L;
        MockHttpServletRequest request = createAuthenticatedRequest(userId);

        // 락 획득 실패 (이미 분석 중)
        given(lockManager.tryLock(anyString())).willReturn(false);

        // when & then
        assertThatThrownBy(() -> analysisService.analyze(url, request))
                .isInstanceOf(BusinessException.class);

        then(repositoryService).shouldHaveNoInteractions();
        then(evaluationService).shouldHaveNoInteractions();
    }

    @Test
    @DisplayName("analyze → Repository 수집 실패 시에도 락 해제")
    void analyze_repositoryFetchFails_releasesLock() {
        // given
        String url = "https://github.com/owner/repo";
        Long userId = 1L;
        MockHttpServletRequest request = createAuthenticatedRequest(userId);

        given(lockManager.tryLock(anyString())).willReturn(true);
        given(repositoryService.fetchAndSaveRepository("owner", "repo", userId))
                .willThrow(new RuntimeException("GitHub API 실패"));

        // when & then
        assertThatThrownBy(() -> analysisService.analyze(url, request))
                .isInstanceOf(RuntimeException.class);

        // 예외 발생해도 락은 해제되어야 함
        then(lockManager).should().releaseLock(anyString());
    }

    @Test
    @DisplayName("analyze → Evaluation 실패 시에도 락 해제")
    void analyze_evaluationFails_releasesLock() {
        // given
        String url = "https://github.com/owner/repo";
        Long userId = 1L;
        MockHttpServletRequest request = createAuthenticatedRequest(userId);

        given(lockManager.tryLock(anyString())).willReturn(true);

        RepositoryData fakeData = createMinimal();
        fakeData.setRepositoryUrl("https://github.com/owner/repo");
        given(repositoryService.fetchAndSaveRepository("owner", "repo", userId))
                .willReturn(fakeData);

        Repositories fakeRepo = mock(Repositories.class);
        given(fakeRepo.getId()).willReturn(1L);
        given(repositoryJpaRepository.findByHtmlUrlAndUserId(anyString(), anyLong()))
                .willReturn(Optional.of(fakeRepo));

        // evaluateAndSave에서 예외 발생
        willThrow(new RuntimeException("OpenAI API 실패"))
                .given(evaluationService).evaluateAndSave(any(), anyLong());

        // when & then
        assertThatThrownBy(() -> analysisService.analyze(url, request))
                .isInstanceOf(RuntimeException.class);

        // 예외 발생해도 락은 해제되어야 함
        then(lockManager).should().releaseLock(anyString());
    }
}