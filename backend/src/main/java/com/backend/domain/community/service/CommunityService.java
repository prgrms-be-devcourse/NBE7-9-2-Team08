package com.backend.domain.community.service;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.repository.repository.RepositoryRepository;
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
}
