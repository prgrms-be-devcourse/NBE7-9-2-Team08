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
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

import java.util.ArrayList;
import java.util.List;

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
        Long jwtUserId = jwtUtil.getUserId(httpRequest);

        Long repositoryId  = analysisService.analyze(request.githubUrl(), jwtUserId);
        AnalysisStartResponse response = new AnalysisStartResponse(repositoryId);

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // GET: 사용자의 모든 Repository 목록 조회
    @GetMapping("/{userId}/repositories")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<List<RepositoryResponse>>> getMemberHistory(
            @PathVariable Long userId,
            HttpServletRequest httpRequest
    ){
        Long jwtUserId = jwtUtil.getUserId(httpRequest);
        if (!jwtUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        List<RepositoryResponse> repositories = repositoryService
                .findRepositoryByUser(userId)
                .stream()
                .map(RepositoryResponse::new)
                .toList();

        return ResponseEntity.ok(ApiResponse.success(repositories));
    }

    // GET: 특정 Repository의 분석 히스토리 조회, 모든 분석 결과 조회
    @GetMapping("/{userId}/repositories/{repositoriesId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<HistoryResponseDto>> getAnalysisByRepositoriesId(
            @PathVariable("userId") Long userId,
            @PathVariable("repositoriesId") Long repoId,
            HttpServletRequest httpRequest
    ){

        // 1. Repository 정보 조회
        Repositories repository = repositoryService.findById(repoId)
                .orElseThrow(() -> new BusinessException(ErrorCode.GITHUB_REPO_NOT_FOUND));

        RepositoryResponse repositoryResponse = new RepositoryResponse(repository);

        // 권한 검증
        Long jwtUserId = jwtUtil.getUserId(httpRequest);
        boolean isOwner = jwtUserId.equals(userId);
        boolean isPublic = repository.isPublicRepository();

        if (!isOwner && !isPublic) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

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

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // GET: 특정 분석 결과 상세 조회
    @GetMapping("/{userId}/repositories/{repositoryId}/results/{analysisId}")
    @Transactional(readOnly = true)
    public ResponseEntity<ApiResponse<AnalysisResultResponseDto>> getAnalysisDetail(
            @PathVariable Long userId,
            @PathVariable Long repositoryId,
            @PathVariable Long analysisId,
            HttpServletRequest httpRequest
    ) {
        // 분석 결과 조회
        AnalysisResult analysisResult = analysisService.getAnalysisById(analysisId);

        if (!analysisResult.getRepositories().getId().equals(repositoryId)) {
            throw new BusinessException(ErrorCode.INVALID_INPUT_VALUE);
        }
        
        // 권한 검증
        Long jwtUserId = jwtUtil.getUserId(httpRequest);
        Repositories repository = analysisResult.getRepositories();
        boolean isOwner = jwtUserId.equals(userId);
        boolean isPublic = repository.isPublicRepository();

        if (!isOwner && !isPublic) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        AnalysisResultResponseDto response =
                new AnalysisResultResponseDto(analysisResult, analysisResult.getScore());

        return ResponseEntity.ok(ApiResponse.success(response));
    }

    // Repository 삭제
    @DeleteMapping("/{userId}/repositories/{repositoriesId}")
    public ResponseEntity<ApiResponse<Void>> deleteRepository(
            @PathVariable("repositoriesId") Long repositoriesId,
            @PathVariable Long userId,
            HttpServletRequest httpRequest){
        Long jwtUserId = jwtUtil.getUserId(httpRequest);
        if (!jwtUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        analysisService.delete(repositoriesId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 특정 AnalysisResult 삭제
    @DeleteMapping("/{userId}/repositories/{repositoryId}/results/{analysisId}")
    public ResponseEntity<ApiResponse<Void>> deleteAnalysisResult(
            @PathVariable Long userId,
            @PathVariable Long repositoryId,
            @PathVariable Long analysisId,
            HttpServletRequest httpRequest){
        Long jwtUserId = jwtUtil.getUserId(httpRequest);
        if (!jwtUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        analysisService.deleteAnalysisResult(analysisId, repositoryId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 분석 결과 공개 여부 변경
    @PutMapping("/{userId}/repositories/{repositoryId}/public")
    public ResponseEntity<ApiResponse<Void>> updatePublicStatus(
            @PathVariable Long userId,
            @PathVariable Long repositoryId,
            HttpServletRequest httpRequest){
        Long jwtUserId = jwtUtil.getUserId(httpRequest);
        if (!jwtUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        analysisService.updatePublicStatus(repositoryId, userId);
        return ResponseEntity.ok(ApiResponse.success());
    }

    // 분석 현황 Sse
    @GetMapping("/stream/{userId}")
    public SseEmitter stream(@PathVariable Long userId,
                             HttpServletRequest httpRequest){
        Long jwtUserId = jwtUtil.getUserId(httpRequest);
        if (!jwtUserId.equals(userId)) {
            throw new BusinessException(ErrorCode.FORBIDDEN);
        }

        return analysisProgressService.connect(userId);
    }
}
