package com.backend.domain.community;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.analysis.repository.ScoreRepository;
import com.backend.domain.community.entity.Comment;
import com.backend.domain.community.repository.CommentRepository;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.domain.repository.repository.RepositoryLanguageRepository;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import com.backend.domain.user.util.JwtUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class CommunityControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RepositoryJpaRepository repositoryJpaRepository;

    @Autowired
    private AnalysisResultRepository analysisResultRepository;

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private ScoreRepository scoreRepository;

    @Autowired
    private RepositoryLanguageRepository repositoryLanguageRepository;

    @Autowired
    private EntityManager em;

    @MockBean
    private JwtUtil jwtUtil; // JWT 인증 Mock

    private User testUser;
    private Repositories testRepo;
    private AnalysisResult testAnalysis;

    @BeforeEach
    void setup() {
        // ✅ 자식 먼저 삭제
        commentRepository.deleteAllInBatch();
        scoreRepository.deleteAllInBatch();
        repositoryLanguageRepository.deleteAllInBatch();
        analysisResultRepository.deleteAllInBatch();
        repositoryJpaRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        when(jwtUtil.getUserId(any())).thenReturn(1L);

        testUser = userRepository.save(new User("tester@example.com", "1234", "테스터"));
        testRepo = repositoryJpaRepository.save(Repositories.builder()
                .user(testUser)
                .name("test-repo")
                .description("테스트용 리포지토리입니다.")
                .htmlUrl("https://github.com/test/test-repo")
                .mainBranch("main")
                .publicRepository(true)
                .build());
        testAnalysis = analysisResultRepository.save(AnalysisResult.builder()
                .repositories(testRepo)
                .summary("요약")
                .strengths("강점")
                .improvements("개선점")
                .createDate(LocalDateTime.now())
                .build());
    }



    // 🔹 댓글 작성
    @Test
    @DisplayName("댓글 작성 → DB에 실제 저장 확인")
    @Transactional(propagation = Propagation.NOT_SUPPORTED) // ✅ rollback 비활성화
    void writeComment_success() throws Exception {
        String requestBody = """
                {
                  "memberId": 1,
                  "comment": "통합 테스트 댓글입니다."
                }
                """;

        mockMvc.perform(post("/api/community/" + testAnalysis.getId() + "/write")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.comment").value("통합 테스트 댓글입니다."));

        Comment saved = commentRepository.findTopByOrderByIdDesc().orElseThrow();
        assertThat(saved.getComment()).isEqualTo("통합 테스트 댓글입니다.");
        assertThat(saved.isDeleted()).isFalse();
    }

    // 🔹 댓글 조회 (SoftDelete 제외)
    @Test
    @DisplayName("댓글 조회 → SoftDelete(false) 댓글만 반환된다")
    void getComments_success() throws Exception {
        commentRepository.save(Comment.builder()
                .analysisResult(testAnalysis)
                .memberId(testUser.getId())
                .comment("첫 댓글")
                .deleted(false)
                .build());
        commentRepository.save(Comment.builder()
                .analysisResult(testAnalysis)
                .memberId(testUser.getId())
                .comment("삭제된 댓글")
                .deleted(true)
                .build());

        mockMvc.perform(get("/api/community/" + testAnalysis.getId() + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].comment").value("첫 댓글"))
                .andExpect(jsonPath("$.content[0].deleted").value(false));
    }

    // 🔹 댓글 페이징 조회
    @Test
    @DisplayName("댓글 페이징 조회 → 지정된 크기만 반환된다")
    void getComments_paging_success() throws Exception {
        for (int i = 1; i <= 7; i++) {
            commentRepository.save(Comment.builder()
                    .analysisResult(testAnalysis)
                    .memberId(testUser.getId())
                    .comment("댓글 " + i)
                    .deleted(false)
                    .build());
        }

        mockMvc.perform(get("/api/community/" + testAnalysis.getId() + "/comments")
                        .param("page", "0")
                        .param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(5))
                .andExpect(jsonPath("$.totalElements").value(7))
                .andExpect(jsonPath("$.content[0].comment").value("댓글 7"));
    }

    // 🔹 댓글 수정
    @Test
    @DisplayName("댓글 수정 → 내용이 변경된다")
    void modifyComment_success() throws Exception {
        Comment comment = commentRepository.save(Comment.builder()
                .analysisResult(testAnalysis)
                .memberId(testUser.getId())
                .comment("기존 댓글")
                .deleted(false)
                .build());

        mockMvc.perform(patch("/api/community/modify/" + comment.getId() + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newComment\": \"수정된 댓글\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("댓글 수정 완료"));

        Comment updated = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(updated.getComment()).isEqualTo("수정된 댓글");
    }

    // 🔹 댓글 삭제 (Soft Delete)
    @Test
    @DisplayName("댓글 삭제 → SoftDelete로 deleted=true로 변경된다")
    void deleteComment_success() throws Exception {
        Comment comment = commentRepository.save(Comment.builder()
                .analysisResult(testAnalysis)
                .memberId(testUser.getId())
                .comment("삭제 대상 댓글")
                .deleted(false)
                .build());

        mockMvc.perform(delete("/api/community/delete/" + comment.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("댓글 삭제 완료"));

        em.flush();
        em.clear();

        Comment deleted = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(deleted.isDeleted()).isTrue();
    }

    // 🔹 공개 리포지토리 조회
    @Test
    @DisplayName("공개 리포지토리 조회 → 정상 응답")
    void getPublicRepositories_success() throws Exception {
        mockMvc.perform(get("/api/community/repositories"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].repositoryName").value("test-repo"))
                .andExpect(jsonPath("$.content[0].publicStatus").value(true));
    }

    @Test
    @DisplayName("댓글 조회 시 deleted=true인 댓글은 제외된다")
    void getComments_excludeDeleted() throws Exception {
        commentRepository.save(Comment.builder()
                .analysisResult(testAnalysis)
                .memberId(testUser.getId())
                .comment("보이는 댓글")
                .deleted(false)
                .build());
        commentRepository.save(Comment.builder()
                .analysisResult(testAnalysis)
                .memberId(testUser.getId())
                .comment("삭제된 댓글")
                .deleted(true)
                .build());

        mockMvc.perform(get("/api/community/" + testAnalysis.getId() + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].comment").value("보이는 댓글"));
    }

    @Test
    @DisplayName("댓글 조회 페이징 → 요청한 페이지 크기(size)에 맞게 반환된다")
    void getComments_pagination() throws Exception {
        // given: 댓글 3개 저장
        for (int i = 1; i <= 3; i++) {
            commentRepository.save(Comment.builder()
                    .analysisResult(testAnalysis)
                    .memberId(testUser.getId())
                    .comment("댓글 " + i)
                    .deleted(false)
                    .build());
        }

        // when & then
        mockMvc.perform(get("/api/community/" + testAnalysis.getId() + "/comments?page=0&size=2"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))  // ✅ 한 페이지당 2개만 반환
                .andExpect(jsonPath("$.totalElements").value(3))
                .andExpect(jsonPath("$.totalPages").value(2));
    }
}
