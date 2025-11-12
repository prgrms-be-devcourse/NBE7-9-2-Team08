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
import org.springframework.data.domain.*;

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
                .name("Repo1").htmlUrl("url1").publicRepository(true).build();
        Repositories repo2 = Repositories.builder()
                .name("Repo2").htmlUrl("url2").publicRepository(true).build();

        when(repositoryJpaRepository.findByPublicRepository(true))
                .thenReturn(List.of(repo1, repo2));

        // when
        List<Repositories> result = communityService.getRepositoriesPublicTrue();

        // then
        assertEquals(2, result.size());
        verify(repositoryJpaRepository, times(1)).findByPublicRepository(true);
    }

    @Test
    @DisplayName("커뮤니티 분석결과 조회 - 공개 레포지토리가 없으면 빈 리스트 반환")
    void getCommunityRepositoryList_empty() {
        when(repositoryJpaRepository.findByPublicRepository(true))
                .thenReturn(Collections.emptyList());

        List<Repositories> result = communityService.getRepositoriesPublicTrue();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repositoryJpaRepository, times(1)).findByPublicRepository(true);
    }

    // ------------------------------------------------------
    // 댓글 조회 테스트
    // ------------------------------------------------------

    @Test
    @DisplayName("댓글 조회 성공 - SoftDelete 적용 (deleted=false) 조건으로 최신순 반환")
    void getComments_success() {
        // given
        AnalysisResult analysisResult = AnalysisResult.builder().id(1L).build();

        Comment comment1 = Comment.builder()
                .id(1L).analysisResult(analysisResult)
                .memberId(1L).comment("first").deleted(false).build();

        Comment comment2 = Comment.builder()
                .id(2L).analysisResult(analysisResult)
                .memberId(1L).comment("second").deleted(false).build();

        when(commentRepository.findByAnalysisResultIdAndDeletedOrderByIdDesc(1L, false))
                .thenReturn(List.of(comment2, comment1)); // 최신순

        // when
        List<Comment> result = communityService.getCommentsByAnalysisResult(1L);

        // then
        assertEquals(2, result.size());
        assertEquals("second", result.get(0).getComment());
        assertEquals("first", result.get(1).getComment());
        verify(commentRepository, times(1))
                .findByAnalysisResultIdAndDeletedOrderByIdDesc(1L, false);
    }

    @Test
    @DisplayName("댓글 조회 (SoftDelete 적용) - 존재하지 않는 분석결과 ID일 경우 빈 리스트 반환")
    void getComments_empty() {
        when(commentRepository.findByAnalysisResultIdAndDeletedOrderByIdDesc(99L, false))
                .thenReturn(Collections.emptyList());

        List<Comment> result = communityService.getCommentsByAnalysisResult(99L);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(commentRepository, times(1))
                .findByAnalysisResultIdAndDeletedOrderByIdDesc(99L, false);
    }

    @Test
    @DisplayName("댓글 조회 - 페이징 적용, deleted=false 조건으로 조회")
    void getComments_paging_success() {
        // given
        AnalysisResult analysisResult = AnalysisResult.builder().id(1L).build();

        Comment c1 = Comment.builder().id(1L).comment("one").deleted(false).build();
        Comment c2 = Comment.builder().id(2L).comment("two").deleted(false).build();
        List<Comment> list = List.of(c2, c1);

        Page<Comment> page = new PageImpl<>(list);
        Pageable pageable = PageRequest.of(0, 2, Sort.by("id").descending());

        when(commentRepository.findByAnalysisResultIdAndDeletedOrderByIdDesc(1L, false, pageable))
                .thenReturn(page);

        // when
        Page<Comment> result = communityService.getPagedCommentsByAnalysisResult(1L, 0, 2);

        // then
        assertEquals(2, result.getContent().size());
        assertEquals("two", result.getContent().get(0).getComment());
        verify(commentRepository, times(1))
                .findByAnalysisResultIdAndDeletedOrderByIdDesc(1L, false, pageable);
    }

    // ------------------------------------------------------
    // 댓글 작성 테스트
    // ------------------------------------------------------

    @Test
    @DisplayName("댓글 작성 성공 - 정상 데이터로 댓글 저장")
    void writeComment_success() {
        // given
        AnalysisResult analysisResult = AnalysisResult.builder().id(1L).build();
        when(analysisResultRepository.findById(1L))
                .thenReturn(Optional.of(analysisResult));

        Comment saved = Comment.builder()
                .id(1L).analysisResult(analysisResult)
                .memberId(1L).comment("write ok").deleted(false).build();
        when(commentRepository.save(any(Comment.class))).thenReturn(saved);

        // when
        Comment result = communityService.addComment(1L, 1L, "write ok");

        // then
        assertNotNull(result);
        assertEquals("write ok", result.getComment());
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 작성 실패 - AnalysisResult 없음")
    void writeComment_noAnalysisResult_throws() {
        when(analysisResultRepository.findById(1L))
                .thenReturn(Optional.empty());

        assertThrows(BusinessException.class,
                () -> communityService.addComment(1L, 1L, "내용"));
        verify(commentRepository, never()).save(any());
    }

    @Test
    @DisplayName("댓글 작성 실패 - 내용 비어있음")
    void writeComment_emptyContent_throws() {
        AnalysisResult ar = AnalysisResult.builder().id(1L).build();
        when(analysisResultRepository.findById(1L))
                .thenReturn(Optional.of(ar));

        assertThrows(BusinessException.class,
                () -> communityService.addComment(1L, 1L, ""));
    }

    // ------------------------------------------------------
    // 댓글 수정 테스트
    // ------------------------------------------------------

    @Test
    @DisplayName("댓글 수정 성공 - updateComment 호출됨")
    void modifyComment_success() {
        Comment comment = mock(Comment.class);
        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment));

        communityService.modifyComment(1L, "new text");

        verify(commentRepository, times(1)).findById(1L);
        verify(comment, times(1)).updateComment("new text");
    }

    @Test
    @DisplayName("댓글 수정 실패 - 존재하지 않으면 예외 발생")
    void modifyComment_notFound_throws() {
        when(commentRepository.findById(1L))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> communityService.modifyComment(1L, "new text"));

        assertEquals(ErrorCode.COMMENT_NOT_FOUND, ex.getErrorCode());
    }

    // ------------------------------------------------------
    // 댓글 삭제 테스트 (Soft Delete)
    // ------------------------------------------------------

    @Test
    @DisplayName("댓글 삭제 성공 - 존재하는 댓글 SoftDelete 처리")
    void deleteComment_success() {
        Comment comment = Comment.builder()
                .id(1L).comment("target").deleted(false).build();
        when(commentRepository.findByIdAndDeleted(1L, false))
                .thenReturn(Optional.of(comment));

        communityService.deleteComment(1L);

        verify(commentRepository, times(1)).findByIdAndDeleted(1L, false);
        verify(commentRepository, times(1)).delete(comment);
    }

    @Test
    @DisplayName("댓글 삭제 실패 - 존재하지 않으면 예외 발생")
    void deleteComment_notFound_throws() {
        when(commentRepository.findByIdAndDeleted(1L, false))
                .thenReturn(Optional.empty());

        BusinessException ex = assertThrows(BusinessException.class,
                () -> communityService.deleteComment(1L));

        assertEquals(ErrorCode.COMMENT_NOT_FOUND, ex.getErrorCode());
        verify(commentRepository, never()).delete(any());
    }
}
