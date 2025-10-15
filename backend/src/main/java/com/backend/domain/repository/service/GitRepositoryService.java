package com.backend.domain.repository.service;

import com.backend.domain.repository.entity.GitRepository;
import com.backend.domain.repository.repository.GitRepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GitRepositoryService {
    private final GitRepositoryRepository gitRepositoryRepository;

    // Repository에서 member로 리포지토리 찾기
    public List<GitRepository> findRepositoryByMember(Long memberId){
        return gitRepositoryRepository.findByMember_Id(memberId);
    }
}
