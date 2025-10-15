package com.backend.domain.community.service;

import com.backend.domain.repository.entity.AnalysisResult;
import com.backend.domain.repository.repository.AnalysisResultRepository;
import com.backend.domain.repository.repository.LanguageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CommunityService {
    private final AnalysisResultRepository analysisResultRepository;
    private final LanguageRepository languageRepository;

    // 내 분석 히스토리 : 회원 Id로 조회, pageSize = 10
    public Page<AnalysisResultRepository> getMyRepository (Long memberId, Pageable pagable){
        Page<AnalysisResult> analysisResult =

        return analysisResultRepository.findAllById(memberId);
    }
}
