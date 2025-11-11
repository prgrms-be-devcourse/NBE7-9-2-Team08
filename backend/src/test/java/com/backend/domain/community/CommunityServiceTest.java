package com.backend.domain.community;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.community.entity.Comment;
import com.backend.domain.community.repository.CommentRepository;
import com.backend.domain.community.service.CommunityService;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.global.exception.BusinessException;
import com.backend.global.exception.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CommunityServiceTest {

    @Mock
    private RepositoryJpaRepository repositoryJpaRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AnalysisResultRepository analysisResultRepository;

    @InjectMocks
    private CommunityService communityService;

    // ------------------------------------------------------
    // 커뮤니티 내 분석결과 조회 테스트
    // ------------------------------------------------------

    @Test
    @DisplayName("커뮤니티 분석결과 조회 성공 - 공개된 리포지토리 목록 반환")
    void getCommunityRepositoryList_success() {
        // given
        Repositories repo1 = Repositories.builder()
                .name("Repo1")
                .htmlUrl("url1")
                .publicRepository(true)
                .build();

        Repositories repo2 = Repositories.builder()
                .name("Repo2")
                .htmlUrl("url2")
                .publicRepository(true)
                .build();

        when(repositoryJpaRepository.findByPublicRepository(true))
                .thenReturn(List.of(repo1, repo2));

        // when
        List<Repositories> result = communityService.getRepositoriesPublicTrue();

        // then
        assertEquals(2, result.size());
        assertEquals("Repo1", result.get(0).getName());
        assertEquals("Repo2", result.get(1).getName());
        verify(repositoryJpaRepository, times(1)).findByPublicRepository(true);
    }

    @Test
    @DisplayName("커뮤니티 분석결과 조회 - 공개 레포지토리가 없으면 빈 리스트 반환")
    void getCommunityRepositoryList_empty() {
        // given
        when(repositoryJpaRepository.findByPublicRepository(true))
                .thenReturn(Collections.emptyList());

        // when
        List<Repositories> result = communityService.getRepositoriesPublicTrue();

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repositoryJpaRepository, times(1)).findByPublicRepository(true);
    }

    // ------------------------------------------------------
    // 댓글 조회 테스트
    // ------------------------------------------------------

    @Test
    @DisplayName("댓글 조회 성공 - analysisResultId 기준으로 최신순 반환")
    void getComments_success() {
        // given
        AnalysisResult analysisResult = AnalysisResult.builder()
                .id(1L)
                .build();

        Comment comment1 = Comment.builder()
                .id(1L)
                .analysisResult(analysisResult)
                .memberId(1L)
                .comment("test comment 1")
                .build();

        Comment comment2 = Comment.builder()
                .id(2L)
                .analysisResult(analysisResult)
                .memberId(1L)
                .comment("test comment 2")
                .build();

        when(commentRepository.findByAnalysisResultIdAndDeletedOrderByIdDesc(1L, true))
                .thenReturn(List.of(comment1, comment2));

        // when
        List<Comment> result = communityService.getCommentsByAnalysisResult(1L);

        // then
        assertEquals(2, result.size());
        assertEquals("test comment 1", result.get(0).getComment());
        assertEquals("test comment 2", result.get(1).getComment());
        verify(commentRepository, times(1)).findByAnalysisResultIdAndDeletedOrderByIdDesc(1L, true);
    }

    @Test
    @DisplayName("댓글 조회 - 존재하지 않는 분석결과 ID일 경우 빈 리스트 반환")
    void getComments_empty() {
        // given
        when(commentRepository.findByAnalysisResultIdAndDeletedOrderByIdDesc(99L, true))
                .thenReturn(Collections.emptyList());

        // when
        List<Comment> result = communityService.getCommentsByAnalysisResult(99L);

        // then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(commentRepository, times(1)).findByAnalysisResultIdAndDeletedOrderByIdDesc(99L, true);
    }

    // ------------------------------------------------------
    // 댓글 작성 테스트
    // ------------------------------------------------------

    @Test
    @DisplayName("댓글 작성 성공 - 정상 데이터로 댓글 저장")
    void writeComment_success() {
        // given
        AnalysisResult analysisResult = AnalysisResult.builder()
                .id(1L)
                .build();

        when(analysisResultRepository.findById(1L))
                .thenReturn(Optional.of(analysisResult));

        Comment savedComment = Comment.builder()
                .id(1L)
                .analysisResult(analysisResult)
                .memberId(1L)
                .comment("write Test 1")
                .build();

        when(commentRepository.save(any(Comment.class))).thenReturn(savedComment);

        // when
        Comment result = communityService.addComment(1L, 1L, "write Test 1");

        // then
        assertNotNull(result);
        assertEquals(1L, result.getMemberId());
        assertEquals("write Test 1", result.getComment());
        assertEquals(1L, result.getAnalysisResult().getId());
        verify(analysisResultRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 작성 실패 - 분석 결과가 존재하지 않으면 예외 발생")
    void writeComment_noAnalysisResult_throwsException() {
        // given
        when(analysisResultRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class,
                () -> communityService.addComment(1L, 1L, "테스트 댓글"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 작성 실패 - 내용이 비어 있을 경우 예외 발생")
    void writeComment_emptyContent_throwsException() {
        // given
        AnalysisResult analysisResult = AnalysisResult.builder()
                .id(1L)
                .build();
        when(analysisResultRepository.findById(1L))
                .thenReturn(Optional.of(analysisResult));

        // when & then
        assertThrows(BusinessException.class,
                () -> communityService.addComment(1L, 1L, ""));
    }

    // ------------------------------------------------------
    // 댓글 수정 테스트
    // ------------------------------------------------------

    @Test
    @DisplayName("댓글 수정 성공 - 존재하는 댓글 내용 변경")
    void modifyComment_success() {
        // given
        Comment comment = Comment.builder()
                .id(1L)
                .comment("old content")
                .build();

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment));

        // when
        communityService.modifyComment(1L, "update content");

        // then
        assertEquals("update content", comment.getComment());
        verify(commentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("댓글 수정 실패 - 존재하지 않는 댓글일 경우 예외 발생")
    void modifyComment_notFound_throwsException() {
        // given
        when(commentRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> communityService.modifyComment(1L, "update content")
        );

        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
        verify(commentRepository, times(1)).findById(1L);
    }

    @Test
    @DisplayName("댓글 수정 시 Comment.updateComment()가 한 번 호출된다")
    void modifyComment_callsUpdateMethod_once() {
        // given
        Comment comment = mock(Comment.class);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when
        communityService.modifyComment(1L, "update content");

        // then
        verify(comment, times(1)).updateComment("update content");
    }

    // ------------------------------------------------------
    // 댓글 삭제 테스트
    // ------------------------------------------------------

    @Test
    @DisplayName("댓글 삭제 성공 - 존재하는 댓글 삭제")
    void deleteComment_success() {
        // given
        Comment comment = Comment.builder()
                .id(1L)
                .comment("target content")
                .build();
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when
        communityService.deleteComment(1L);

        // then
        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 존재하지 않는 댓글 ID일 경우 예외 발생")
    void deleteComment_notFound_throwsException() {
        // given
        when(commentRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> communityService.deleteComment(1L)
        );

        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, never()).delete(any());
    }
}
