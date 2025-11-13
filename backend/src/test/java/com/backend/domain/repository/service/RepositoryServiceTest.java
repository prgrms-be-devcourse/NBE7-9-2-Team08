package com.backend.domain.repository.service;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.domain.user.repository.UserRepository;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import com.backend.domain.user.entity.User;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Slf4j
@SpringBootTest
@Transactional
class RepositoryServiceTest {

    @Autowired
    private RepositoryService repositoryService;

    @Autowired
    private RepositoryJpaRepository repositoryJpaRepository;

    @Autowired
    private UserRepository userRepository;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User(
                "test" + System.currentTimeMillis() + "@test.com",
                "password123",
                "Test User"
        );
        testUser = userRepository.save(testUser);
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures1() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team01";
        Long userId = testUser.getId();

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrlAndUserId(data.getRepositoryUrl(), userId);
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures2() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team02";
        Long userId = testUser.getId();

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrlAndUserId(data.getRepositoryUrl(), userId);
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures3() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team3";
        Long userId = testUser.getId();

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrlAndUserId(data.getRepositoryUrl(), userId);
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures4() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team04";
        Long userId = testUser.getId();

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrlAndUserId(data.getRepositoryUrl(), userId);
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures5() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team05";
        Long userId = testUser.getId();

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrlAndUserId(data.getRepositoryUrl(), userId);
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures6() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team06";
        Long userId = testUser.getId();

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrlAndUserId(data.getRepositoryUrl(), userId);
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures7() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team07";
        Long userId = testUser.getId();

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrlAndUserId(data.getRepositoryUrl(), userId);
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures8() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team08";
        Long userId = testUser.getId();

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrlAndUserId(data.getRepositoryUrl(), userId);
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures9() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team9";
        Long userId = testUser.getId();

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrlAndUserId(data.getRepositoryUrl(), userId);
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures10() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team10";
        Long userId = testUser.getId();

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrlAndUserId(data.getRepositoryUrl(), userId);
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("빈 리포지토리라도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures11() {
        // given
        String owner = "Hyeseung-OH";
        String repo = "test";
        Long userId = testUser.getId();

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrlAndUserId(data.getRepositoryUrl(), userId);
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("완벽한 README + 테스트 + CI/CD를 갖춘 저장소")
    void testWellStructuredRepository() {
        // given
        String owner = "spring-projects";
        String repo = "spring-boot";
        Long userId = testUser.getId();

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        assertThat(data.isHasReadme()).isTrue();
        assertThat(data.getReadmeLength()).isGreaterThan(1000);
        assertThat(data.isHasTestDirectory()).isTrue();
        assertThat(data.isHasCICD()).isTrue();
        assertThat(data.getTestCoverageRatio()).isGreaterThan(0.0);
    }

    @Test
    @DisplayName("용량 큰 저장소의 경우 분석 불가")
    void testActiveRepository() {
        // given
        String owner = "facebook";
        String repo = "react";
        Long userId = testUser.getId();

        // when & then
        assertThatThrownBy(() -> repositoryService.fetchAndSaveRepository(owner, repo, userId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GITHUB_REPO_TOO_LARGE);

        log.info("✅ 용량 큰 저장소(1_000_000KB 이상)는 분석 불가능함 (GITHUB_REPO_TOO_LARGE)");
    }

    @Test
    @DisplayName("특수문자가 포함된 저장소명도 처리 가능")
    void testRepositoryNameWithSpecialChars() {
        // given - 실제로는 GitHub가 허용하는 특수문자만 가능 (-, _, .)
        String owner = "day8";
        String repo = "re-frame";
        Long userId = testUser.getId();

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
    }

    @Test
    @DisplayName("존재하지 않는 저장소 요청 시 BusinessException(GITHUB_REPO_NOT_FOUND) 발생")
    void testRepositoryNotFound() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team0";
        Long userId = testUser.getId();

        // when & then
        assertThatThrownBy(() -> repositoryService.fetchAndSaveRepository(owner, repo, userId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GITHUB_REPO_NOT_FOUND);

        log.info("✅ 존재하지 않는 저장소는 정상적으로 예외 발생함 (GITHUB_REPO_NOT_FOUND)");
    }
}
