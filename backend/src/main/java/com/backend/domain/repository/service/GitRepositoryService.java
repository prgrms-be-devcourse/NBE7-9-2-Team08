package com.backend.domain.repository.service;

import com.backend.domain.repository.entity.GitRepository;
import com.backend.domain.repository.entity.Language;
import com.backend.domain.repository.entity.RepositoryLanguage;
import com.backend.domain.repository.repository.GitRepositoryRepository;
import com.backend.domain.repository.repository.LanguageRepository;
import com.backend.domain.repository.repository.RepositoryLanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GitRepositoryService {
    private final GitRepositoryRepository gitRepositoryRepository;
    private final RepositoryLanguageRepository repositoryLanguageRepository;
    private final LanguageRepository languageRepository;

    // Repository에서 member로 리포지토리 찾기
    public List<GitRepository> findRepositoryByMember(Long memberId){
        return gitRepositoryRepository.findByMember_Id(memberId);
    }

    // GitRepository ID로 언어 조회
    public List<Language> findLanguagesByRepisotryId(Long gitRepositoryId){
        return repositoryLanguageRepository.findLanguagesByGitRepositoryId(gitRepositoryId);
    }

}
