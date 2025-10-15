package com.backend.domain.analysis.service;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.repository.entity.Repository;
import com.backend.domain.repository.repository.RepositoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional
public class AnalysisResultService {
    private final AnalysisResultRepository analysisResultRepository;
    private final RepositoryRepository repositoryRepository;

    
    public Optional<AnalysisResult> findAnalysisResultByRepositoryId (Long repositoryId){
        return analysisResultRepository.findByRepositoryId(repositoryId);
    }

    public List<Repository> findRepositoryByMemberId (Long memberId){
        return repositoryRepository.findByMemberId(memberId);
    }
}
