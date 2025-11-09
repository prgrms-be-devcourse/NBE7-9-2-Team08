package com.backend.domain.analysis.controller;

import com.backend.domain.analysis.dto.request.AnalysisRequest;
import com.backend.domain.analysis.dto.response.AnalysisResultResponseDto;
import com.backend.domain.analysis.dto.response.AnalysisStartResponse;
import com.backend.domain.analysis.dto.response.HistoryResponseDto;
import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.service.AnalysisProgressService;
import com.backend.domain.analysis.service.AnalysisService;
import com.backend.domain.repository.dto.response.RepositoryResponse;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.service.RepositoryService;
import com.backend.domain.user.util.JwtUtil;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import com.backend.global.response.ApiResponse;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AnalysisService analysisService;
    private final RepositoryService repositoryService;
    private final AnalysisProgressService analysisProgressService;
    private final JwtUtil jwtUtil;

    // POST: 분석 요청
    @PostMapping
    public ResponseEntity<ApiResponse<AnalysisStartResponse>> analyzeRepository(
            @RequestBody AnalysisRequest request,
            HttpServletRequest httpRequest
    ) {
        if (request == null) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }

        Long repositoryId  = analysisService.analyze(request.githubUrl(), httpRequest);
        AnalysisStartResponse response = new AnalysisStartResponse(repositoryId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // GET: 사용자의 모든 Repository 목록 조회
    @GetMapping("/repositories")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<RepositoryResponse>>> getMemberHistory(
            HttpServletRequest httpRequest
    ){
        List<RepositoryResponse> repositories = repositoryService.getUserRepositories(httpRequest);
        return ResponseEntity.ok(ApiResponse.success(repositories));
    }

    // GET: 특정 Repository의 분석 히스토리 조회, 모든 분석 결과 조회
    @GetMapping("/repositories/{repositoriesId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<HistoryResponseDto>> getAnalysisByRepositoriesId(
            @PathVariable("repositoriesId") Long repoId,
            HttpServletRequest httpRequest
    ){
        HistoryResponseDto response = analysisService.getHistory(repoId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // GET: 특정 분석 결과 상세 조회
    @GetMapping("/repositories/{repositoryId}/results/{analysisId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<AnalysisResultResponseDto>> getAnalysisDetail(
            @PathVariable Long repositoryId,
            @PathVariable Long analysisId,
            HttpServletRequest httpRequest
    ) {
        AnalysisResultResponseDto response = analysisService.getAnalysisDetail(
                repositoryId,
                analysisId,
                httpRequest
        );
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Repository 삭제
    @DeleteMapping("/{userId}/repositories/{repositoriesId}")
    public ResponseEntity<ApiResponse<Void>> deleteRepository(
            @PathVariable("repositoriesId") Long repositoriesId,
            @PathVariable Long userId,
            HttpServletRequest httpRequest){
        analysisService.delete(repositoriesId, userId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 특정 AnalysisResult 삭제
    @DeleteMapping("/{userId}/repositories/{repositoryId}/results/{analysisId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnalysisResult(
            @PathVariable Long userId,
            @PathVariable Long repositoryId,
            @PathVariable Long analysisId,
            HttpServletRequest httpRequest){
        analysisService.deleteAnalysisResult(analysisId, repositoryId, userId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 분석 결과 공개 여부 변경
    @PutMapping("/{userId}/repositories/{repositoryId}/public")
    public ResponseEntity<ApiResponse<Void>> updatePublicStatus(
            @PathVariable Long userId,
            @PathVariable Long repositoryId,
            HttpServletRequest httpRequest){
        analysisService.updatePublicStatus(repositoryId, userId, httpRequest);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 분석 현황 Sse
    @GetMapping("/stream/{userId}")
    public SseEmitter stream(@PathVariable Long userId,
                             HttpServletRequest httpRequest){
        return analysisProgressService.connect(userId, httpRequest);
    }
}
