package com.backend.domain.analysis.controller;

import com.backend.domain.analysis.dto.HistoryResponseDto;
import com.backend.domain.analysis.dto.request.AnalysisRequest;
import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;
import com.backend.domain.analysis.service.AnalysisService;
import com.backend.domain.repository.entity.Language;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.entity.RepositoryLanguage;
import com.backend.domain.repository.service.RepositoryService;
import com.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AnalysisService analysisService;
    private final RepositoryService repositoryService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> analyzeRepository(@RequestBody AnalysisRequest request) {
        analysisService.analyze(request.githubUrl());
        return ResponseEntity.ok(ApiResponse.success());
    }

    @GetMapping("/user/{memberId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<HistoryResponseDto>> getMemberHistory(@PathVariable Long memberId){
        List<Repositories> repositories = repositoryService.findRepositoryByMember(memberId);
        List<HistoryResponseDto> historyList = new ArrayList<>();

        for (Repositories repo : repositories) {
            Optional<AnalysisResult> optionalAnalysis = analysisService.findAnalysisResultByRepositoryId(repo.getId());

            if (optionalAnalysis.isPresent()) { // 존재하는지 확인
                AnalysisResult analysisResult = optionalAnalysis.get();
                Score score = analysisResult.getScore();

                List<String> languages = repositoryService.findLanguagesByRepisotryId(repo.getId())
                        .stream()
                        .map(Enum::name) // RepositoryLanguage -> Language enum
                        .toList();

                HistoryResponseDto dto = new HistoryResponseDto(repo, analysisResult, score, languages);
                historyList.add(dto);
            }
        }

        // 최신순 정렬
        historyList.sort((a, b) -> b.createDate().compareTo(a.createDate()));

        return ResponseEntity.ok(historyList);
    }
}
