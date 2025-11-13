package com.backend.domain.evaluation.service;

import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import jakarta.mail.MessagingException;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.core.env.Environment;              // ★ 추가
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assumptions.assumeTrue;
import static org.mockito.Mockito.mock;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class EvaluationServiceExternalIT {

    @TestConfiguration
    static class MailStubConfig {
        @Bean @Primary
        JavaMailSender javaMailSender() { return mock(JavaMailSender.class); }
    }

    @Autowired private EvaluationService evaluationService;
    @Autowired private UserRepository userRepository;
    @Autowired private RepositoryJpaRepository repositoryJpaRepository;
    @Autowired private AnalysisResultRepository analysisResultRepository;

    @Autowired private Environment env;                // ★ Spring Environment 주입

    private String repoUrl;
    private Long userId = 1L;

    @BeforeEach
    void seed() {
        // 1) 우리 프로젝트 방식(.env → Environment)으로 키 확인
        String key = env.getProperty("openai.api.key");       // DotenvEnvironmentPostProcessor가 여길 채움
        if (key == null || key.isBlank()) {
            key = env.getProperty("OPENAI_API_KEY");          // 보조 키 이름도 체크
        }
        assumeTrue(key != null && !key.isBlank(),
                "openai.api.key / OPENAI_API_KEY 가 없어 외부 호출 테스트를 건너뜁니다.");

        // 2) 선행 데이터(유저/레포)
        User user = userRepository.save(new User("ext-test@example.com", "pw", "ext"));
        repoUrl = "https://github.com/test-owner/test-repo";
        repositoryJpaRepository.save(
                Repositories.builder()
                        .user(user)
                        .name("test-repo")
                        .description("external openai test")
                        .htmlUrl(repoUrl)
                        .publicRepository(true)
                        .mainBranch("main")
                        .build()
        );
    }

    @Test
    @DisplayName("실제 OpenAI 호출되어 토큰 소비되는 외부 연동 테스트")
    void evaluateAndSave_realOpenAI_call() throws MessagingException {
        RepositoryData data = new RepositoryData();
        data.setRepositoryUrl(repoUrl);

        Long id = evaluationService.evaluateAndSave(data, userId);
        assertThat(id).isNotNull();
        assertThat(analysisResultRepository.findById(id)).isPresent();
    }
}
