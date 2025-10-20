package com.backend.domain.analysis.controller;

import com.backend.domain.analysis.dto.response.AnalysisResultResponseDto;
import com.backend.domain.analysis.dto.response.HistoryResponseDto;
import com.backend.domain.analysis.dto.request.AnalysisRequest;
import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;
import com.backend.domain.analysis.service.AnalysisService;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.service.RepositoryService;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
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

    // POST: 분석 요청
    @PostMapping
    public ResponseEntity<ApiResponse<Void>> analyzeRepository(@RequestBody AnalysisRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        analysisService.analyze(request.githubUrl());
        return ResponseEntity.ok(ApiResponse.success());
    }

    // GET: 사용자 히스토리 전체 목록 조회
    @GetMapping("/{memberId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<HistoryResponseDto>> getMemberHistory(@PathVariable Long memberId){
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<Repositories> repositories = repositoryService.findRepositoryByMember(memberId);
        List<HistoryResponseDto> historyList = new ArrayList<>();

        for (Repositories repo : repositories) {
            Optional<AnalysisResult> optionalAnalysis = analysisService.findByRepositoryId(repo.getId());

            if (optionalAnalysis.isEmpty()) {
                continue;
            }

            AnalysisResult analysisResult = optionalAnalysis.get();
            Score score = analysisResult.getScore();

            List<String> languages = repositoryService.findLanguagesByRepisotryId(repo.getId())
                    .stream()
                    .map(Enum::name) // RepositoryLanguage -> Language enum
                    .toList();

            HistoryResponseDto dto = new HistoryResponseDto(repo, analysisResult, score, languages);
            historyList.add(dto);
        }

        // 최신순 정렬
        historyList.sort((a, b) -> b.createDate().compareTo(a.createDate()));

        return ResponseEntity.ok(historyList);
    }

    // GET: 사용자 분석 결과 조회
    @GetMapping("/{memberId}/{repositoriesId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<AnalysisResultResponseDto>> getAnalysisByRepositoriesId(
            @PathVariable("memberId") Long memberId,
            @PathVariable("repositoriesId") Long repoId
    ){
        List<AnalysisResult> optionalResult = analysisService.getAnalysisResultList(repoId);
        List<AnalysisResultResponseDto> resultList = new ArrayList<>();


        for(AnalysisResult result : optionalResult){
            Score score = result.getScore();
            AnalysisResultResponseDto dto = new AnalysisResultResponseDto(result, score);
            resultList.add(dto);
        }

        return ResponseEntity.ok(resultList);
    }
}
