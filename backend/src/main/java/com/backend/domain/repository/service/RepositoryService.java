package com.backend.domain.repository.service;

import com.backend.domain.repository.dto.response.RepositoryData;
import com.backend.domain.repository.dto.response.github.RepoResponse;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.domain.repository.service.fetcher.FetcherRepository;
import com.backend.domain.repository.service.mapper.MapperRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class RepositoryService {

    private final FetcherRepository fetcherRepository;
    private final MapperRepository mapperRepository;
    private final RepositoryJpaRepository repositoryJpaRepository;

    @Transactional
    public RepositoryData fetchAndSaveRepository(String owner, String repo) {
        RepoResponse response = fetcherRepository.fetchRepositoryInfo(owner, repo);

        Repositories entity = mapperRepository.toEntity(response);
        Repositories repositories = repositoryJpaRepository
                .findByHtmlUrl(entity.getHtmlUrl())
                .map(existing -> {
                    existing.updateFrom(entity);
                    return existing;
                })
                .orElseGet(() -> repositoryJpaRepository.save(entity));

        RepositoryData data = mapperRepository.toRepositoryData(response);
        log.info("RepositoryData 결과: {}", data);

        return data;
    }
}
