package com.backend.domain.community.controller;

import com.backend.domain.analysis.dto.response.AnalysisResultResponseDto;
import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;
import com.backend.domain.analysis.service.AnalysisService;
import com.backend.domain.community.dto.CommunityResponseDto;
import com.backend.domain.community.service.CommunityService;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.service.RepositoryService;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/communtiy")
public class CommunityController {
    private final CommunityService communityService;
    private final AnalysisService analysisService;
    private final RepositoryService repositoryService;
    private final UserService userService;

    // publisRepositories = true (공개여부 : 공개함) 리포지토리 조회
    @GetMapping("/reposioties")
    public ResponseEntity<List<CommunityResponseDto>> getPublicRepositories(){
        List<Repositories> repositories = communityService.getCommunityRepository();
        List<CommunityResponseDto> publicRepositories = new ArrayList<>();

        for(Repositories repo : repositories) {
            Optional<AnalysisResult> optionalAnalysis = analysisService.findByRepositoryId(repo.getId());
            if (optionalAnalysis.isPresent()) {
                AnalysisResult analysisResult = optionalAnalysis.get();
                Score score = analysisResult.getScore();

                List<String> languages = repositoryService.findLanguagesByRepisotryId(repo.getId())
                        .stream()
                        .map(Enum::name) // RepositoryLanguage -> Language enum
                        .toList();

                User user = repositoryService.findUserByRepositoriesId(repo.getId());

                CommunityResponseDto dto = new CommunityResponseDto(user.getName(), repo.getName(), analysisResult.getSummary(), languages, score.getTotalScore());
                publicRepositories.add(dto);
            }

        }

        return ResponseEntity.ok(publicRepositories);
    }
}
