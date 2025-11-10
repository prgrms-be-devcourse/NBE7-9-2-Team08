package com.backend.domain.community;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.analysis.repository.AnalysisResultRepository;
import com.backend.domain.community.entity.Comment;
import com.backend.domain.community.repository.CommentRepository;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.repository.repository.RepositoryJpaRepository;
import com.backend.domain.user.entity.User;
import com.backend.domain.user.repository.UserRepository;
import com.backend.domain.user.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
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
class CommunityControllerIntegrationTest {

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

    @MockBean
    private JwtUtil jwtUtil; // ✅ JWT 인증 우회용 MockBean

    private User testUser;
    private Repositories testRepo;
    private AnalysisResult testAnalysis;

    @BeforeEach
    void setup() {
        // ✅ 테스트 DB 초기화 (데이터 누적 방지)
        commentRepository.deleteAll();
        analysisResultRepository.deleteAll();
        repositoryJpaRepository.deleteAll();
        userRepository.deleteAll();

        // ✅ Mock JWT 설정 (항상 로그인된 유저로 처리)
        when(jwtUtil.getUserId(any())).thenReturn(1L);

        // ✅ 테스트 유저 생성
        testUser = userRepository.save(new User("tester@example.com", "1234", "테스터"));

        // ✅ 테스트 리포지토리 생성
        testRepo = repositoryJpaRepository.save(Repositories.builder()
                .user(testUser)
                .name("test-repo")
                .description("테스트용 리포지토리입니다.")
                .htmlUrl("https://github.com/test/test-repo")
                .mainBranch("main")
                .publicRepository(true)
                .build());

        // ✅ 테스트 분석 결과 생성
        testAnalysis = analysisResultRepository.save(AnalysisResult.builder()
                .repositories(testRepo)
                .summary("요약")
                .strengths("장점")
                .improvements("개선점")
                .createDate(LocalDateTime.now())
                .build());
    }

    // 🔹 댓글 작성
    @Test
    @DisplayName("댓글 작성 → DB에 실제 저장 확인")
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

        // ✅ DB 검증: 새로 추가된 댓글만 확인
        Comment saved = commentRepository.findTopByOrderByIdDesc().orElseThrow();
        assertThat(saved.getComment()).isEqualTo("통합 테스트 댓글입니다.");
        assertThat(saved.getAnalysisResult().getId()).isEqualTo(testAnalysis.getId());
    }

    // 🔹 댓글 조회
    @Test
    @DisplayName("댓글 조회 → 저장된 댓글이 반환된다")
    void getComments_success() throws Exception {
        commentRepository.save(Comment.builder()
                .analysisResult(testAnalysis)
                .memberId(testUser.getId())
                .comment("조회 테스트 댓글")
                .build());

        mockMvc.perform(get("/api/community/" + testAnalysis.getId() + "/comments"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].comment").value("조회 테스트 댓글"));
    }

    // 🔹 댓글 수정
    @Test
    @DisplayName("댓글 수정 → 내용이 변경된다")
    void modifyComment_success() throws Exception {
        Comment comment = commentRepository.save(Comment.builder()
                .analysisResult(testAnalysis)
                .memberId(testUser.getId())
                .comment("기존 댓글")
                .build());

        mockMvc.perform(patch("/api/community/modify/" + comment.getId() + "/comment")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"newComment\": \"수정된 댓글\"}"))
                .andExpect(status().isOk())
                .andExpect(content().string("댓글 수정 완료"));

        Comment updated = commentRepository.findById(comment.getId()).orElseThrow();
        assertThat(updated.getComment()).isEqualTo("수정된 댓글");
    }

    // 🔹 댓글 삭제
    @Test
    @DisplayName("댓글 삭제 → DB에서 제거 확인")
    void deleteComment_success() throws Exception {
        Comment comment = commentRepository.save(Comment.builder()
                .analysisResult(testAnalysis)
                .memberId(testUser.getId())
                .comment("삭제 대상 댓글")
                .build());

        mockMvc.perform(delete("/api/community/delete/" + comment.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("댓글 삭제 완료"));

        assertThat(commentRepository.existsById(comment.getId())).isFalse();
    }

    // 🔹 공개 리포지토리 조회
    @Test
    @DisplayName("공개 리포지토리 조회 → 정상 응답")
    void getPublicRepositories_success() throws Exception {
        mockMvc.perform(get("/api/community/repositories"))
                .andExpect(status().isOk());
    }
}
