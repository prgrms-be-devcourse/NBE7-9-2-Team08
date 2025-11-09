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
public class CommunityServiceTest {

    @Mock
    private RepositoryJpaRepository repositoryJpaRepository;

    @Mock
    private CommentRepository commentRepository;

    @Mock
    private AnalysisResultRepository analysisResultRepository;

    @InjectMocks
    private CommunityService communityService;


    /*
     *  커뮤니티 내에서 분석결과를 조회하는 내용에 대한 테스트 입니다.
     */
    @Test
    @DisplayName("커뮤니티 분석결과 조회 테스트")
    void getCommunityRepositoryList(){

        // 테스트 데이터 만들기
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

        // 리포지토리에서 함수를 호출할 때 해당 결과로 반환하도록 하기
        when(repositoryJpaRepository.findByPublicRepository(true))
                .thenReturn(List.of(repo1, repo2));


        // Service 코드로 조회 하기
        // NPE 발생 :
        List<Repositories> result = communityService.getRepositoriesPublicTrue();

        // 호출한 내용과 위 데이터 내용이 같은지 확인
        assertEquals(2, result.size());
        assertEquals("Repo1", result.get(0).getName());
        assertEquals("Repo2", result.get(1).getName());

        // repository가 정확한지 확인
        verify(repositoryJpaRepository, times(1)).findByPublicRepository(true);
    }

    @Test
    @DisplayName("조회 시, 데이터가 null이 출력될 경우")
    void AnalysisResultListIsNull(){
        when(repositoryJpaRepository.findByPublicRepository(true))
                .thenReturn(Collections.emptyList());

        List<Repositories> result = communityService.getRepositoriesPublicTrue();

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(repositoryJpaRepository, times(1)).findByPublicRepository(true);
    }

    /*
     *  분석결과 당 댓글을 조회하는 내용에 대한 테스트 입니다.
     */
    @Test
    @DisplayName("댓글 조회 테스트")
    void getCommnets(){
        // 테스트 데이터 만들기
        AnalysisResult analysisResult1 = AnalysisResult.builder()
                .id(1L)
                .build();

        Comment comment1 = Comment.builder()
                .id(1L)
                .analysisResult(analysisResult1) // 가짜 analysisResult 넣어주기
                .memberId(1L)
                .comment("test comment 1")
                .build();

        Comment comment2 = Comment.builder()
                .id(2L)
                .memberId(1L)
                .comment("test comment 2")
                .build();

        // 리포지토리에서 서비스 함수를 호출할 때 테스트 데이터를 반환하도록 하기
        when(commentRepository.findByAnalysisResultIdOrderByIdDesc(1L))
                .thenReturn(List.of(comment1, comment2));

        // 서비스 코드로 데이터 조회
        List<Comment> result = communityService.getCommentsByAnalysisResult(1L);

        assertEquals(2, result.size());
        assertEquals("test comment 1", result.get(0).getComment());
        assertEquals("test comment 2", result.get(1).getComment());

        verify(commentRepository, times(1))
                .findByAnalysisResultIdOrderByIdDesc(1L);
    }

    @Test
    @DisplayName("댓글이 없을 경우 빈 리스트 반환")
    void getComments_noComments() {
        // given
        when(commentRepository.findByAnalysisResultIdOrderByIdDesc(99L))
                .thenReturn(Collections.emptyList());

        // when
        List<Comment> result = communityService.getCommentsByAnalysisResult(99L);

        // then
        assertNotNull(result);
        assertEquals(0, result.size());
        verify(commentRepository, times(1))
                .findByAnalysisResultIdOrderByIdDesc(99L);
    }

    @Test
    @DisplayName("댓글 작성 시 AnalysisResult가 존재하지 않으면 예외 발생")
    void writeComment_noAnalysisResult() {
        // given
        when(analysisResultRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(
                BusinessException.class, // 또는 NoSuchElementException 등
                () -> communityService.addComment(1L, 1L, "테스트 댓글"),
                "AnalysisResult not found 예외가 발생해야 함"
        );

        verify(analysisResultRepository, times(1)).findById(1L);
        verify(commentRepository, never()).save(any());
    }


    /*
     *  분석결과 당 댓글을 작성하는 내용에 대한 테스트 입니다.
     */
    @Test
    @DisplayName("댓글 작성 테스트")
    void writeComment(){
        // 작성 한 내용
        AnalysisResult analysisResult1 = AnalysisResult.builder()
                .id(1L)
                .build();

        when(analysisResultRepository.findById(1L))
                .thenReturn(Optional.of(analysisResult1)); // findById는 Optional이라서 null / 결과를 반환 -> 여기선 확실하ㅔㄱ null을 반환하니까 테스트용 데이터를 반환해라

        Comment comment1 = Comment.builder()
                .id(1L)
                .analysisResult(analysisResult1)
                .memberId(1L)
                .comment("write Test 1")
                .build();

        when(commentRepository.save(any(Comment.class)))
                .thenReturn(comment1);

        // 작성
        Comment writeResult = communityService.addComment(1L, 1L, "write Test 1");

        // 작성 후 내용이 작성 했던 내용이 그대로 들어왔는지 확인
        assertNotNull(writeResult);
        assertEquals(1L, writeResult.getMemberId());
        assertEquals("write Test 1", writeResult.getComment());
        assertEquals(1L, writeResult.getAnalysisResult().getId());

        verify(analysisResultRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).save(any(Comment.class));
    }

    @Test
    @DisplayName("댓글 작성 실패 - 분석 결과가 없으면 BusinessException 발생")
    void writeComment_noAnalysisResult_throwsBusinessException() {
        // given
        when(analysisResultRepository.findById(anyLong()))
                .thenReturn(Optional.empty());

        // when & then
        assertThrows(BusinessException.class,
                () -> communityService.addComment(1L, 1L, "Write Test 1"));

        verify(analysisResultRepository, times(1)).findById(1L);
        verify(commentRepository, never()).save(any(Comment.class)); // 저장 시도 X
    }

    @Test
    @DisplayName("댓글 작성 실패 - 내용이 비어 있을 경우 예외 발생")
    void writeComment_emptyContent_throwsException() {
        // mock repository에 넣어둘 가짜 데이터
        AnalysisResult analysisResult1 = AnalysisResult.builder()
                .id(1L)
                .build();

        when(analysisResultRepository.findById(1L))
                .thenReturn(Optional.of(analysisResult1));

        // mock이 .save()를 호출했을 때 반환해줄 가짜 데이터
        Comment savedComment = Comment.builder()
                .id(1L)
                .analysisResult(analysisResult1)
                .memberId(1L)
                .comment("테스트 댓글")
                .build();

//        when(commentRepository.save(any(Comment.class)))  // 예외처리로 .save가 사용되지 않음으로 해당 내용 주석처리
//                .thenReturn(savedComment);

        // 댓글 작성 시,  댓글 내용이 없을 때 -> 예외처리 확인
        assertThrows(BusinessException.class,
                () -> communityService.addComment(1L, 1L, ""));
    }

    /*
     *  분석결과 당 댓글을 수정하는 내용에 대한 테스트 입니다.
     */
    @Test
    @DisplayName("댓글 수정 성공 - 존재하는 댓글의 내용이 변경된다")
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
    @DisplayName("댓글 수정 실패 - 존재하지 않는 댓글 ID일 경우 예외 발생")
    void modifyComment_notFound_throwsBusinessException() {
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
    @DisplayName("댓글 수정 시 updateComment()가 한 번 호출된다")
    void modifyComment_callsUpdateComment_once() {
        // given
        Comment comment = mock(Comment.class);
        when(commentRepository.findById(1L)).thenReturn(Optional.of(comment));

        // when
        communityService.modifyComment(1L, "update content");

        // then
        verify(comment, times(1)).updateComment("update content");
    }

    /*
     *  분석결과 당 댓글을 삭제하는 내용에 대한 테스트 입니다.
     */
    @Test
    @DisplayName("댓글 삭제 성공 확인")
    void deleteCommnet(){
        Comment comment1 = Comment.builder()
                .id(1L)
                .comment("target content")
                .build();

        when(commentRepository.findById(1L))
                .thenReturn(Optional.of(comment1));

        communityService.deleteComment(1L);

        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, times(1)).delete(comment1);
    }

    @Test
    @DisplayName("댓글 삭제 시, 존재하지 않는 댓글일 때 예외처리 반환 확인")
    void notFoundComment(){
        // given
        when(commentRepository.findById(1L))
                .thenReturn(Optional.empty());

        // when & then
        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> communityService.deleteComment(1L)
        );

        // then
        assertEquals(ErrorCode.COMMENT_NOT_FOUND, exception.getErrorCode());
        verify(commentRepository, times(1)).findById(1L);
        verify(commentRepository, never()).delete(any());
    }
}
