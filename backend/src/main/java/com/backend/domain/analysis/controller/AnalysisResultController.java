package com.backend.domain.analysis.controller;

import com.backend.domain.analysis.dto.HistoryResponseDto;
import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.service.AnalysisResultService;
import com.backend.domain.repository.entity.GitRepository;
import com.backend.domain.repository.service.GitRepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;


@RestController
@RequiredArgsConstructor
@Transactional
public class AnalysisResultController {

    private final GitRepositoryService gitRepositoryService;
    private final AnalysisResultService analysisResultService;

    @GetMapping("/member/{memberId}")
    @Transactional(readOnly = true)
    public ResponseEntity<List<HistoryResponseDto>> getMemberHistory(@PathVariable Long memberId){
        List<GitRepository> repositories = gitRepositoryService.findRepositoryByMember(memberId);

        List<HistoryResponseDto> historyList = new ArrayList<>();

        for (GitRepository repo : repositories) {
            Optional<AnalysisResult> optionalAnalysis = analysisResultService.findAnalysisResultByRepositoryId(repo.getId());

            if (optionalAnalysis.isPresent()) {
                AnalysisResult ar = optionalAnalysis.get();
                HistoryResponseDto dto = new HistoryResponseDto(repo, ar);
                historyList.add(dto);
            }
        }

        return ResponseEntity.ok(historyList);
    }
}
