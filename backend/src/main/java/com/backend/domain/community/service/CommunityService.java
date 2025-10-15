package com.backend.domain.community.service;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.repository.repository.RepositoryRepository;
import com.backend.domain.repository.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {
    private final RepositoryRepository repositoryRepository;
    private final LanguageRepository languageRepository;

    // 내 분석 히스토리 : 회원 Id로 조회, pageSize = 10
    public Page<RepositoryRepository> getMyRepository (Long memberId, Pageable pagable){
        Page<AnalysisResult> analysisResult =

        return repositoryRepository.findAllById(memberId);
    }
}
