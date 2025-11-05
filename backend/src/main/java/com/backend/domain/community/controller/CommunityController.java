package com.backend.domain.community.controller;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;
import com.backend.domain.analysis.service.AnalysisService;
import com.backend.domain.community.dto.request.CommentRequestDto;
import com.backend.domain.community.dto.request.CommentUpdateRequestDto;
import com.backend.domain.community.dto.response.CommentResponseDto;
import com.backend.domain.community.dto.response.CommentWriteResponseDto;
import com.backend.domain.community.dto.response.CommunityResponseDto;
import com.backend.domain.community.entity.Comment;
import com.backend.domain.community.service.CommunityService;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.service.UserService;
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
    private final UserService userService;

    /**
     * 커뮤니티 관련 기능이 있는 컨트롤러 입니다.
     * - 공개 리포지토리 조회
     * - 분석 결과 댓글 작성 / 조회 / 삭제/ 수정
     *
     */

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

                CommunityResponseDto dto = new CommunityResponseDto(repo, analysisResult, score);
                communityRepositories.add(dto);
            }
        }

        communityRepositories.sort((a, b) -> b.createDate().compareTo(a.createDate()));

    return ResponseEntity.ok(communityRepositories);
    }



    // 댓글 작성
    @PostMapping("/{analysisResultId}/write")
    public ResponseEntity<CommentWriteResponseDto> addComment(
            @PathVariable Long analysisResultId,
            @RequestBody CommentRequestDto requestDto
    ) {
        Comment saved = communityService.addComment(
                analysisResultId,
                requestDto.memberId(),
                requestDto.comment()
        );
        return ResponseEntity.ok(new CommentWriteResponseDto(saved));
    }

    // 댓글 조회
    @GetMapping("/{analysisResultId}/comments")
    public ResponseEntity<List<CommentResponseDto>> getCommentsByAnalysisResult(
            @PathVariable Long analysisResultId
    ) {
        List<Comment> comments = communityService.getCommentsByAnalysisResult(analysisResultId);
        List<CommentResponseDto> commentList = new ArrayList<>();

        for(Comment comment : comments){
            User userName = userService.getUserNameByUserId(comment.getMemberId());

            CommentResponseDto dto = new CommentResponseDto(comment, userName.getName());
            commentList.add(dto);
        }

        commentList.sort((a, b) -> b.id().compareTo(a.id()));
        return ResponseEntity.ok(commentList);
    }

    // 댓글 삭제
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<String> deleteCommnt(
            @PathVariable Long commentId
    ){
        communityService.deleteComment(commentId);
        return ResponseEntity.ok("댓글 삭제 완료");
    }

    // 댓글 수정
    @PatchMapping("/modify/{commentId}/comment")
    public ResponseEntity<String> modifyComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequestDto updateDto
            ){
        communityService.modifyComment(commentId, updateDto.newComment());
        return ResponseEntity.ok("댓글 수정 완료");
    }
}
