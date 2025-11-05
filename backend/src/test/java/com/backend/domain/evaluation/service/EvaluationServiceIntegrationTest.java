package com.backend.domain.evaluation.service;

import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.analysis.repository.ScoreRepository;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EvaluationServiceIntegrationTest {

    @Autowired private EvaluationService evaluationService;
    @Autowired private UserRepository userRepository;
    @Autowired private RepositoryJpaRepository repositoryJpaRepository;
    @Autowired private AnalysisResultRepository analysisResultRepository;
    @Autowired private ScoreRepository scoreRepository;

    @MockitoBean
    private AiService aiService; // OpenAI 호출만 목킹

    private String repoUrl;

    @BeforeEach
    void seed() {
        // 1) 유저 시드
        User user = userRepository.save(new User("tester@example.com", "pw", "tester"));

        // 2) Repositories 시드 (evaluation이 repo를 찾을 때 htmlUrl로 매칭)
        repoUrl = "https://github.com/test-owner/test-repo";
        Repositories repo = Repositories.builder()
                .user(user)
                .name("test-repo")
                .description("just for evaluation test")
                .htmlUrl(repoUrl)
                .publicRepository(true)
                .mainBranch("main")
                .build();
        repositoryJpaRepository.save(repo);
    }

//    @Test
//    @DisplayName("evaluateAndSave(RepositoryData) → AnalysisResult/Score 실제 저장")
//    void evaluateAndSave_saves() {
//        // 3) RepositoryData 준비 (url만 맞으면 됨; 나머지는 평가 프롬프트에 영향 없도록 최소)
//        RepositoryData data = new RepositoryData();
//        // RepositoryData에 세터가 있다면:
//        data.setRepositoryUrl(repoUrl);
//
//        // 4) AiService 결과 목킹(JSON 고정)
//        String json = """
//            {
//              "summary": "요약입니다",
//              "strengths": ["README가 충실함", "커밋 메시지가 일관적임"],
//              "improvements": ["테스트 커버리지 보완"],
//              "scores": { "readme": 21, "test": 18, "commit": 20, "cicd": 17 }
//            }
//            """;
//        given(aiService.complete(ArgumentMatchers.any(AiDto.CompleteRequest.class)))
//                .willReturn(new AiDto.CompleteResponse(json));
//
//        // 5) 실행
//        Long analysisId = evaluationService.evaluateAndSave(data);
//
//        // 6) 검증: AnalysisResult/Score가 실제로 저장됐는지
//        AnalysisResult ar = analysisResultRepository.findById(analysisId).orElseThrow();
//        assertThat(ar.getSummary()).contains("요약");
//        assertThat(ar.getStrengths()).contains("README");
//        assertThat(ar.getImprovements()).contains("테스트 커버리지");
//        assertThat(ar.getScore()).isNotNull();
//        assertThat(ar.getScore().getReadmeScore()).isEqualTo(21);
//        assertThat(ar.getScore().getTestScore()).isEqualTo(18);
//        assertThat(ar.getScore().getCommitScore()).isEqualTo(20);
//        assertThat(ar.getScore().getCicdScore()).isEqualTo(17);
//        assertThat(ar.getRepositories().getHtmlUrl()).isEqualTo(repoUrl);
//    }

    @TestConfiguration
    static class MailStubConfig {
        @Bean
        @Primary
        JavaMailSender javaMailSender() {
            // 모든 send(...)가 no-op인 mock
            return org.mockito.Mockito.mock(JavaMailSender.class);
        }
    }
}
