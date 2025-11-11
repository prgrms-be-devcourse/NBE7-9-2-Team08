package com.backend.domain.community;

import com.backend.domain.analysis.entity.AnalysisResult;
import com.backend.domain.community.entity.Comment;
import com.backend.domain.community.repository.CommentRepository;
import com.backend.domain.repository.entity.Repositories;
import com.backend.domain.user.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager em;

    @Test
    @DisplayName("✅ analysisResultId 기준으로 댓글을 ID 내림차순 정렬하여 조회한다")
    void findByAnalysisResultIdOrderByIdDesc_success() {
        // 1️⃣ User 생성 및 저장
        User user = new User("tester@example.com", "1234", "테스터");
        em.persist(user);

        // 2️⃣ Repository 생성 및 저장
        Repositories repo = Repositories.builder()
                .user(user) // ✅ not null
                .name("test-repo")
                .description("테스트용 레포지토리입니다.")
                .htmlUrl("https://github.com/test/test-repo") // ✅ not null
                .publicRepository(true)
                .mainBranch("main")
                .build();
        em.persist(repo);

        // 3️⃣ AnalysisResult 저장
        AnalysisResult analysisResult = AnalysisResult.builder()
                .repositories(repo)
                .summary("요약")
                .strengths("강점")
                .improvements("개선점")
                .createDate(LocalDateTime.now())
                .build();
        em.persist(analysisResult);
        em.flush(); // ✅ DB에 실제로 insert 확정

// 4️⃣ Comment 저장
        Comment comment1 = Comment.builder()
                .comment("첫 번째 댓글")
                .memberId(10L)
                .analysisResult(analysisResult) // ✅ 이미 DB에 있는 AnalysisResult
                .build();

        Comment comment2 = Comment.builder()
                .comment("두 번째 댓글")
                .memberId(20L)
                .analysisResult(analysisResult)
                .build();

        commentRepository.save(comment1);
        commentRepository.save(comment2);

        // when
        List<Comment> result = commentRepository.findByAnalysisResultIdAndDeletedOrderByIdDesc(analysisResult.getId(), true);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getComment()).isEqualTo("두 번째 댓글");
        assertThat(result.get(1).getComment()).isEqualTo("첫 번째 댓글");
    }

    @Test
    @DisplayName("✅ 댓글이 존재하지 않는 경우 빈 리스트를 반환한다")
    void findByAnalysisResultIdOrderByIdDesc_emptyResult() {
        // given: 존재하지 않는 AnalysisResult ID
        Long nonExistentId = 9999L;

        // when
        List<Comment> result = commentRepository.findByAnalysisResultIdAndDeletedOrderByIdDesc(nonExistentId, true);

        // then
        assertThat(result).isEmpty();
    }
}