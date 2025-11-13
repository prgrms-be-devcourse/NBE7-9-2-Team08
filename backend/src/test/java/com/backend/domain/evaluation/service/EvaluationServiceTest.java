package com.backend.domain.evaluation.service;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.evaluation.dto.AiDto;
import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import com.backend.domain.user.service.EmailService;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import static com.backend.domain.repository.dto.RepositoryDataFixture.createMinimal;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EvaluationServiceTest {

    @Autowired private EvaluationService evaluationService;
    @Autowired private RepositoryJpaRepository repositoryJpaRepository;
    @Autowired private AnalysisResultRepository analysisResultRepository;
    @Autowired private UserRepository userRepository;
    @Autowired EntityManager em;

    @MockitoBean
    private AiService aiService; // 실제 OpenAI 호출 막고 고정 JSON 사용

    @MockitoBean
    private EmailService emailService;

    @Test
    @DisplayName("evaluateAndSave(RepositoryData) → AnalysisResult & Score 저장")
    void evaluateAndSave_success() {
        // 1) 사전데이터: User + Repository (DB에 저장)
        User user = new User("test@example.com", "pw", "tester");
        userRepository.save(user);

        String url = "https://github.com/test/hello";
        Repositories repo = Repositories.builder()
                .user(user)
                .name("hello")
                .description("desc")
                .htmlUrl(url)
                .publicRepository(true)
                .mainBranch("main")
                .build();
        repositoryJpaRepository.save(repo);

        // 2) RepositoryData (서비스 입력값)
        RepositoryData data = createMinimal();
        data.setRepositoryName("hello");
        data.setRepositoryUrl(url);
        data.setDescription("desc");

        // 3) AI 결과 Mock
        String json = """
                {
                  "summary": "요약입니다",
                  "strengths": ["A", "B"],
                  "improvements": ["C"],
                  "scores": { "readme": 21, "test": 18, "commit": 20, "cicd": 17 }
                }
                """;
        given(aiService.complete(any(AiDto.CompleteRequest.class)))
                .willReturn(new AiDto.CompleteResponse(json));

        // 4) 실행
        Long id = evaluationService.evaluateAndSave(data, user.getId());
        em.flush();
        em.clear();

        // 5) 검증
        AnalysisResult ar = analysisResultRepository.findById(id).orElseThrow();
        assertThat(ar.getRepositories().getId()).isEqualTo(repo.getId());
        assertThat(ar.getSummary()).contains("요약");
        assertThat(ar.getStrengths()).contains("- A");
        assertThat(ar.getImprovements()).contains("- C");
        assertThat(ar.getScore()).isNotNull();
        assertThat(ar.getScore().getReadmeScore()).isEqualTo(21);
        assertThat(ar.getScore().getTestScore()).isEqualTo(18);
        assertThat(ar.getScore().getCommitScore()).isEqualTo(20);
        assertThat(ar.getScore().getCicdScore()).isEqualTo(17);
    }
}
