package com.backend.domain.analysis.controller;

import com.backend.domain.analysis.dto.HistoryResponseDto;
import com.backend.domain.analysis.service.AnalysisResultService;
import com.backend.domain.repository.entity.GitRepository;
import com.backend.domain.repository.service.GitRepositoryService;
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

    private final GitRepositoryService gitRepositoryService;
    private final AnalysisResultService analysisResultService;

    @GetMapping("/member/{member}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<HistoryResponseDto>> getMemberHistory(@PathVariable Long memberId){
        List<GitRepository> repositories = gitRepositoryService.findRepositoryByMember(memberId);

        List<HistoryResponseDto> historyList = repositories.stream()
                .map(repo -> {
                    return analysisResultService
                            .findAnalysisResultByRepositoryId(repo.getId())
                            .map(ar -> new HistoryResponseDto(repo, ar))
                            .orElse(null);
                })
                .filter(Objects::nonNull)
                .toList();

        return ResponseEntity.ok(historyList);
    }
}
