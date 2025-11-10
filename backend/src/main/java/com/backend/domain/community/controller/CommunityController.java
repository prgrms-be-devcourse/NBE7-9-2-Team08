package com.backend.domain.community.controller;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.entity.Score;
import com.backend.domain.analysis.service.AnalysisService;
import com.backend.domain.community.dto.request.CommentRequestDTO;
import com.backend.domain.community.dto.request.CommentUpdateRequestDTO;
import com.backend.domain.community.dto.response.CommentResponseDTO;
import com.backend.domain.community.dto.response.CommentWriteResponseDTO;
import com.backend.domain.community.dto.response.CommunityResponseDTO;
import com.backend.domain.community.entity.Comment;
import com.backend.domain.community.service.CommunityService;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.service.UserService;
import com.backend.domain.user.util.JwtUtil;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/community")
public class CommunityController {
    private final CommunityService communityService;
    private final AnalysisService analysisService;
    private final UserService userService;
    private final JwtUtil jwtUtil;

    /**
     * 커뮤니티 관련 기능이 있는 컨트롤러 입니다.
     * - 공개 리포지토리 조회
     * - 분석 결과 댓글 작성 / 조회 / 삭제/ 수정
     *
     */

    // publisRepositories = true (공개여부 : 공개함) 리포지토리 조회
    // 공개 리포지토리 조회
    @GetMapping("/repositories")
    public ResponseEntity<Page<CommunityResponseDTO>> getPublicRepositories(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "5") int size
    ) {

        // publicRepository가 true인 리포지토리 조회
        Page<Repositories> publicRepository = communityService.getPagedRepositoriesPublicTrue(page, size);
        List<CommunityResponseDTO> communityRepositories = new ArrayList<>();

        for (Repositories repo : publicRepository) {
            if (repo == null) continue;

            // 분석 결과 조회 (없을 수도 있음)
            List<AnalysisResult> analysisList = analysisService.getAnalysisResultList(repo.getId());
            if (analysisList == null || analysisList.isEmpty()) continue;

            // 가장 첫 번째(가장 최신) 분석 결과만 사용
            AnalysisResult analysisResult = analysisList.get(0);

            // Score가 null일 경우 기본값 생성
            Score score = Optional.ofNullable(analysisResult.getScore())
                    .orElseGet(() -> new Score(analysisResult, 0, 0, 0, 0));

            // DTO 생성 시 NPE 방지
            CommunityResponseDTO dto = new CommunityResponseDTO(repo, analysisResult, score);
            communityRepositories.add(dto);
        }

        // 최신순 정렬
        communityRepositories.sort((a, b) -> b.createDate().compareTo(a.createDate()));

        Page<CommunityResponseDTO> pageingResponseDto = new PageImpl<>(
                communityRepositories,
                publicRepository.getPageable(),
                publicRepository.getTotalElements()
        );

        return ResponseEntity.ok(pageingResponseDto);
    }

    // 분석 결과 당 댓글 조회
    @GetMapping("/{analysisResultId}/comments")
    public ResponseEntity<List<CommentResponseDTO>> getCommentsByAnalysisResult(
            @PathVariable Long analysisResultId
    ) {
        List<Comment> comments = communityService.getCommentsByAnalysisResult(analysisResultId);
        List<CommentResponseDTO> commentList = new ArrayList<>();

        for(Comment comment : comments){
            User userName = userService.getUserNameByUserId(comment.getMemberId());

            CommentResponseDTO dto = new CommentResponseDTO(comment, userName.getName());
            commentList.add(dto);
        }

        commentList.sort((a, b) -> b.commentId().compareTo(a.commentId()));
        return ResponseEntity.ok(commentList);
    }

    // 댓글 작성
    @PostMapping("/{analysisResultId}/write")
    public ResponseEntity<CommentWriteResponseDTO> addComment(
            @PathVariable Long analysisResultId,
            @RequestBody CommentRequestDTO requestDto,
            HttpServletRequest httpRequest
    ) {
        Long jwtUserId = jwtUtil.getUserId(httpRequest);

        if(jwtUserId == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_USER);
        }

        Comment saved = communityService.addComment(
                analysisResultId,
                requestDto.memberId(),
                requestDto.comment()
        );
        return ResponseEntity.ok(new CommentWriteResponseDTO(saved));
    }

    // 댓글 삭제
    @DeleteMapping("/delete/{commentId}")
    public ResponseEntity<String> deleteCommnt(
            @PathVariable Long commentId,
            HttpServletRequest httpRequest
    ){
        Long jwtUserId = jwtUtil.getUserId(httpRequest);

        if(jwtUserId == null){
            throw new BusinessException(ErrorCode.NOT_LOGIN_USER);
        }

        communityService.deleteComment(commentId);
        return ResponseEntity.ok("댓글 삭제 완료");
    }

    // 댓글 수정
    @PatchMapping("/modify/{commentId}/comment")
    public ResponseEntity<String> modifyComment(
            @PathVariable Long commentId,
            @RequestBody CommentUpdateRequestDTO updateDto
            ){
        communityService.modifyComment(commentId, updateDto.newComment());
        return ResponseEntity.ok("댓글 수정 완료");
    }
}
