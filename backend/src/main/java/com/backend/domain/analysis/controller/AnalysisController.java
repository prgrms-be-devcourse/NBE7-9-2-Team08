package com.backend.domain.analysis.controller;

import com.backend.domain.analysis.dto.request.AnalysisRequest;
import com.backend.domain.analysis.dto.response.AnalysisResultResponseDto;
import com.backend.domain.analysis.dto.response.HistoryResponseDto;
import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.service.AnalysisService;
import com.backend.domain.repository.dto.response.RepositoryResponse;
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

    // GET: 사용자의 모든 Repository 목록 조회
    @GetMapping("/{memberId}/repositories")
    @Transactional(readOnly = true)
    public ResponseEntity<List<RepositoryResponse>> getMemberHistory(
            @PathVariable Long memberId
    ){
        if (memberId == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        List<RepositoryResponse> repositories = repositoryService
                .findRepositoryByMember(memberId)
                .stream()
                .map(RepositoryResponse::new)
                .toList();

        return ResponseEntity.ok(repositories);
    }

    // GET: 특정 Repository의 분석 히스토리 조회, 모든 분석 결과 조회
    @GetMapping("/{memberId}/repositories/{repositoriesId}")
    @Transactional(readOnly = true)
    public ResponseEntity<HistoryResponseDto> getAnalysisByRepositoriesId(
            @PathVariable("memberId") Long memberId,
            @PathVariable("repositoriesId") Long repoId
    ){
        // TODO: 추후 인증/인가 기능 완성 후 소유권 검증 로직 추가

        // 1. Repository 정보 조회
        Repositories repository = repositoryService.findById(repoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND));

        RepositoryResponse repositoryResponse = new RepositoryResponse(repository);

        // 2. 분석 결과 목록 조회 (최신순 정렬)
        List<AnalysisResult> analysisResults =
                analysisService.getAnalysisResultList(repoId);

        // 3. 버전 DTO 생성 (최신이 가장 큰 번호)
        List<HistoryResponseDto.AnalysisVersionDto> versions = new ArrayList<>();
        int versionNumber = analysisResults.size();

        for (AnalysisResult analysis : analysisResults) {
            versions.add(HistoryResponseDto.AnalysisVersionDto.from(analysis, versionNumber));
            versionNumber--;
        }

        // 4. 응답 조합
        HistoryResponseDto response = HistoryResponseDto.of(repositoryResponse, versions);

        return ResponseEntity.ok(response);
    }

    // GET: 특정 분석 결과 상세 조회
    @GetMapping("/{memberId}/repositories/{repositoryId}/results/{analysisId}")
    @Transactional(readOnly = true)
    public ResponseEntity<AnalysisResultResponseDto> getAnalysisDetail(
            @PathVariable Long memberId,
            @PathVariable Long repositoryId,
            @PathVariable Long analysisId
    ) {
        // TODO: 추후 인증/인가 검증

        // 분석 결과 조회
        AnalysisResult analysisResult = analysisService.getAnalysisById(analysisId);

        if (!analysisResult.getRepositories().getId().equals(repositoryId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        AnalysisResultResponseDto response =
                new AnalysisResultResponseDto(analysisResult, analysisResult.getScore());

        return ResponseEntity.ok(response);
    }
}
