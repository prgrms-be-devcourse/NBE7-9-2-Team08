package com.backend.domain.repository.service;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
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

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures1() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team01";
        Long userId = 1L;

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrl(data.getRepositoryUrl());
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures2() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team02";
        Long userId = 1L;

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrl(data.getRepositoryUrl());
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures3() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team3";
        Long userId = 1L;

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrl(data.getRepositoryUrl());
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures4() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team04";
        Long userId = 1L;

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrl(data.getRepositoryUrl());
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures5() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team05";
        Long userId = 1L;

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrl(data.getRepositoryUrl());
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures6() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team06";
        Long userId = 1L;

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrl(data.getRepositoryUrl());
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures7() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team07";
        Long userId = 1L;

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrl(data.getRepositoryUrl());
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures8() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team08";
        Long userId = 1L;

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrl(data.getRepositoryUrl());
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures9() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team9";
        Long userId = 1L;

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrl(data.getRepositoryUrl());
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("README, TEST, CI/CD가 없어도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures10() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team10";
        Long userId = 1L;

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrl(data.getRepositoryUrl());
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("빈 리포지토리라도 RepositoryData 수집은 정상적으로 완료된다")
    void testRepositoryWithoutSomeFeatures11() {
        // given
        String owner = "Hyeseung-OH";
        String repo = "test";
        Long userId = 1L;

        // when
        RepositoryData data = repositoryService.fetchAndSaveRepository(owner, repo, userId);

        // then
        assertThat(data).isNotNull();
        log.info("📦 수집된 RepositoryData {}:", data);

        // Repositories 저장 확인
        var repoEntity = repositoryJpaRepository.findByHtmlUrl(data.getRepositoryUrl());
        assertThat(repoEntity).isPresent();
    }

    @Test
    @DisplayName("존재하지 않는 저장소 요청 시 BusinessException(GITHUB_REPO_NOT_FOUND) 발생")
    void testRepositoryNotFound() {
        // given
        String owner = "prgrms-be-devcourse";
        String repo = "NBE7-9-2-Team0";
        Long userId = 1L;

        // when & then
        assertThatThrownBy(() -> repositoryService.fetchAndSaveRepository(owner, repo, userId))
                .isInstanceOf(BusinessException.class)
                .extracting("errorCode")
                .isEqualTo(ErrorCode.GITHUB_REPO_NOT_FOUND);

        log.info("✅ 존재하지 않는 저장소는 정상적으로 예외 발생함 (GITHUB_REPO_NOT_FOUND)");
    }
}
