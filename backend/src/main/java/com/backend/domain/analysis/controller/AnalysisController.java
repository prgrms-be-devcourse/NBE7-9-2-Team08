package com.backend.domain.analysis.controller;

import com.backend.domain.analysis.dto.request.AnalysisRequest;
import com.backend.domain.analysis.service.AnalysisService;
import com.backend.global.response.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/analysis")
public class AnalysisController {
    private final AnalysisService analysisService;

    @PostMapping
    public ResponseEntity<ApiResponse<Void>> analyzeRepository(@RequestBody AnalysisRequest request) {
        analysisService.analyze(request.githubUrl());
        return ResponseEntity.ok(ApiResponse.success());
    }
}
