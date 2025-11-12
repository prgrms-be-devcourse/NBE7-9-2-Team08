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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
class CommentRepositoryTest {

    @Autowired
    private CommentRepository commentRepository;

    @Autowired
    private TestEntityManager em;

    private AnalysisResult setupData() {
        User user = new User("tester@example.com", "1234", "테스터");
        em.persist(user);

        Repositories repo = Repositories.builder()
                .user(user)
                .name("test-repo")
                .description("테스트용 레포지토리입니다.")
                .htmlUrl("https://github.com/test/test-repo")
                .publicRepository(true)
                .mainBranch("main")
                .build();
        em.persist(repo);

        AnalysisResult analysisResult = AnalysisResult.builder()
                .repositories(repo)
                .summary("요약")
                .strengths("강점")
                .improvements("개선점")
                .createDate(LocalDateTime.now())
                .build();
        em.persist(analysisResult);
        em.flush();

        return analysisResult;
    }

    @Test
    @DisplayName("✅ 1. analysisResultId 기준으로 삭제되지 않은 댓글을 ID 내림차순 정렬하여 조회한다")
    void findByAnalysisResultIdAndDeletedOrderByIdDesc_success() {
        // given
        AnalysisResult analysisResult = setupData();

        Comment comment1 = Comment.builder()
                .comment("첫 번째 댓글")
                .memberId(10L)
                .analysisResult(analysisResult)
                .deleted(false)
                .build();

        Comment comment2 = Comment.builder()
                .comment("두 번째 댓글")
                .memberId(20L)
                .analysisResult(analysisResult)
                .deleted(false)
                .build();

        commentRepository.save(comment1);
        commentRepository.save(comment2);
        em.flush();

        // when
        List<Comment> result = commentRepository.findByAnalysisResultIdAndDeletedOrderByIdDesc(
                analysisResult.getId(), false);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getComment()).isEqualTo("두 번째 댓글");
        assertThat(result.get(1).getComment()).isEqualTo("첫 번째 댓글");
    }

    @Test
    @DisplayName("✅ 2. 페이징 기반으로 댓글을 조회한다 (삭제되지 않은 것만)")
    void findByAnalysisResultIdAndDeletedOrderByIdDesc_paging() {
        // given
        AnalysisResult analysisResult = setupData();

        for (int i = 1; i <= 5; i++) {
            Comment comment = Comment.builder()
                    .comment("댓글 " + i)
                    .memberId((long) i)
                    .analysisResult(analysisResult)
                    .deleted(false)
                    .build();
            commentRepository.save(comment);
        }
        em.flush();

        Pageable pageable = PageRequest.of(0, 3, Sort.by("id").descending());

        // when
        Page<Comment> pageResult = commentRepository.findByAnalysisResultIdAndDeletedOrderByIdDesc(
                analysisResult.getId(), false, pageable);

        // then
        assertThat(pageResult.getTotalElements()).isEqualTo(5);
        assertThat(pageResult.getContent()).hasSize(3);
        assertThat(pageResult.getContent().get(0).getComment()).isEqualTo("댓글 5");
        assertThat(pageResult.getContent().get(2).getComment()).isEqualTo("댓글 3");
    }

    @Test
    @DisplayName("✅ 3. 댓글 ID와 deleted 상태로 단건 조회 및 soft delete 검증")
    void findByIdAndDeleted_success() {
        // given
        AnalysisResult analysisResult = setupData();

        Comment comment = Comment.builder()
                .comment("삭제 테스트용 댓글")
                .memberId(99L)
                .analysisResult(analysisResult)
                .deleted(false)
                .build();

        commentRepository.save(comment);
        em.flush();

        // when - 삭제 전 조회
        Optional<Comment> found = commentRepository.findByIdAndDeleted(comment.getId(), false);

        // then
        assertThat(found).isPresent();
        assertThat(found.get().getComment()).isEqualTo("삭제 테스트용 댓글");

        // when - soft delete 수행
        commentRepository.delete(comment);
        em.flush();

        // then - 삭제 후 상태 확인
        Optional<Comment> deleted = commentRepository.findByIdAndDeleted(comment.getId(), true);
        assertThat(deleted).isPresent();
        assertThat(deleted.get().isDeleted()).isTrue();
    }
}
