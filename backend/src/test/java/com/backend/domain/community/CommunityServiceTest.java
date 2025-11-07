package com.backend.domain.community;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.community.entity.Comment;
import com.backend.domain.community.repository.CommentRepository;
import com.backend.domain.community.service.CommunityService;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

        assertEquals(1, result.size());
        assertEquals("test comment 1", result.get(0).getComment());

        verify(commentRepository, times(1))
                .findByAnalysisResultIdOrderByIdDesc(1L);
    }

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
}
