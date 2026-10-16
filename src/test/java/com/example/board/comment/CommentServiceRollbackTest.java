package com.example.board.comment;

import com.example.board.member.Member;
import com.example.board.post.domain.Post;
import com.example.board.post.repository.PostRepository;
import com.example.board.support.IntegrationTestSupport;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.willThrow;

@SpringBootTest
public class CommentServiceRollbackTest extends IntegrationTestSupport {

    @Autowired
    CommentService commentService;

    @Autowired
    TransactionTemplate transactionTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    EntityManager em;

    @MockitoBean
    PostRepository postRepository;

    Member member;
    Post post;

    @BeforeEach
    void setUp() {
        transactionTemplate.executeWithoutResult(status -> {
            member = new Member(null, "rollbackTester", "test1234!", "롤백테스터", null, null);
            em.persist(member);
            post = new Post(null, member, "title", "content", null);
            em.persist(post);
        });
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM comment WHERE post_id = ?", post.getId());
        jdbcTemplate.update("DELETE FROM post WHERE post_id = ?", post.getId());
        jdbcTemplate.update("DELETE FROM member WHERE member_id = ?", member.getId());
    }

    @Test
    @DisplayName("댓글 저장 후 댓글 수 증가가 실패하면, 저장했던 댓글도 함꼐 롤백된다")
    void rollbackWhenIncrementFails() {
        willThrow(new DataAccessResourceFailureException("DB error"))
                .given(postRepository).incrementCommentCount(post.getId());

        assertThatThrownBy(() -> commentService.addComment(new Comment(null, post, member, "content")))
                .isInstanceOf(DataAccessResourceFailureException.class);

        Long count = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM comment WHERE post_id = ?", Long.class, post.getId());
        assertThat(count).isZero();
    }

}
