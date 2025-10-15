package com.backend.domain.analysis.controller;

import com.backend.domain.analysis.dto.HistoryResponseDto;
import com.backend.domain.analysis.service.AnalysisResultService;
import com.backend.domain.repository.entity.Repository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Objects;

@RestController
@RequiredArgsConstructor
@Transactional
public class AnalysisResultController {
    private final AnalysisResultService analysisResultService;

    @GetMapping("/history")
    @Transactional(readOnly = true)
    public ResponseEntity<List<HistoryResponseDto>> getMemberHistory(@PathVariable Long memberId){
        List<Repository> repositories = analysisResultService.findRepositoryByMemberId(memberId);

        List<HistoryResponseDto> historyList = repositories.stream()
                .map(repo -> analysisResultService.findAnalysisResultByRepositoryId(repo.getId())
                        .map(result -> new HistoryResponseDto(repo, result))
                        .orElse(null)
                )
                .filter(Objects::nonNull)
                .toList();

        return ResponseEntity.ok(historyList);
    }
}
