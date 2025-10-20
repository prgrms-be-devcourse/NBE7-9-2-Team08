package com.backend.domain.community.controller;

import com.backend.domain.analysis.dto.response.AnalysisResultResponseDto;
import com.backend.domain.analysis.dto.response.HistoryResponseDto;
import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;
import com.backend.domain.analysis.service.AnalysisService;
import com.backend.domain.community.dto.CommunityResponseDto;
import com.backend.domain.community.service.CommunityService;
import com.backend.domain.repository.entity.Language;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.domain.repository.service.RepositoryService;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.service.UserService;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
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
@RequestMapping("/api/community")
public class CommunityController {
    private final CommunityService communityService;
    private final AnalysisService analysisService;
    private final RepositoryService repositoryService;

    // publisRepositories = true (공개여부 : 공개함) 리포지토리 조회
    @GetMapping("/repositories")
    public ResponseEntity<List<CommunityResponseDto>> getPublicRepositories(){
        // publicRepository가 true인 리포지토리 조회
        List<Repositories> publicRepository = communityService.getRepositoriesPublicTrue();
        List<CommunityResponseDto> communityRepositories = new ArrayList<>();

        for(Repositories repo : publicRepository){
            List <AnalysisResult> analysisList = analysisService.getAnalysisResultList(repo.getId());

            if(!(repo == null)){ // 비어있지 않으면

                // 가장 첫번째 값만 사용 : List가 정렬되어서 반환되기 때문에 가장 최신값 사용
                AnalysisResult analysisResult = analysisList.get(0);
                Score score = analysisResult.getScore();

                List<String> languages = repositoryService.getLanguageByRepositoriesId(repo.getId())
                        .stream()
                        .map(Enum::name)
                        .toList();

                CommunityResponseDto dto = new CommunityResponseDto(repo, analysisResult, score);
                communityRepositories.add(dto);
            }
        }

        return ResponseEntity.ok(communityRepositories);
    }
}
