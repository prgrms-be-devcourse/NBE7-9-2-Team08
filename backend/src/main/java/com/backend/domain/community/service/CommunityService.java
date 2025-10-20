package com.backend.domain.community.service;

import com.backend.domain.community.entity.Comment;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {
    private final RepositoryJpaRepository repositoryJpaRepository;

    // publicRepository(repository 필드)가 true인 리포지토리 조회
    public List<Repositories> getRepositoriesPublicTrue(){
        return repositoryJpaRepository.findByPublicRepository(true);
    }
}
