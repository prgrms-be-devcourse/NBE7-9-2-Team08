package com.backend.domain.community.controller;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;
import com.backend.domain.analysis.service.AnalysisService;
import com.backend.domain.community.dto.request.CommentRequestDto;
import com.backend.domain.community.dto.response.CommentResponseDto;
import com.backend.domain.community.dto.response.CommunityResponseDto;
import com.backend.domain.community.entity.Comment;
import com.backend.domain.community.service.CommunityService;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.service.RepositoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;

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

    // 댓글 작성
    @PostMapping("/{analysisResultId}/comments")
    public ResponseEntity<CommentResponseDto> addComment(
            @PathVariable Long analysisResultId,
            @RequestBody CommentRequestDto requestDto
    ) {
        Comment saved = communityService.addComment(
                analysisResultId,
                requestDto.memberId(),
                requestDto.comment()
        );
        return ResponseEntity.ok(new CommentResponseDto(saved));
    }

    // 댓글 조회
    @GetMapping("/{analysisResultId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByAnalysisResult(
            @PathVariable Long analysisResultId
    ) {
        List<Comment> comments = communityService.getCommentsByAnalysisResult(analysisResultId);
        List<CommentResponseDto> response = comments.stream()
                .map(CommentResponseDto::new)
                .toList();

        return ResponseEntity.ok(response);
    }
}
