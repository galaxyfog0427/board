package com.example.board.comment;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class CommentRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    CommentRepository commentRepository;

    @Test
    void crud() {
        Member member = new Member(
                null,
                "commenter",
                "member1234!",
                "댓글러",
                null,
                null);
        Long memberId = memberRepository.save(member).getId();

        Post post = new Post(
                null,
                memberId,
                "제목",
                "내용",
                null
        );
        Long postId = postRepository.save(post).getId();

        Comment comment = new Comment(
                null,
                postId,
                memberId,
                "댓글"
        );
        Long commentId = commentRepository.save(comment).getId();

        Comment foundComment = commentRepository.findById(commentId).get();
        assertThat(foundComment.getContent()).isEqualTo(comment.getContent());
        assertThat(foundComment.getPostId()).isEqualTo(comment.getPostId());
        assertThat(foundComment.getMemberId()).isEqualTo(comment.getMemberId());
    }

    @Test
    @DisplayName("findByPostId()는 먼저 작성된 댓글이 먼저 오도록 정렬한다")
    void findByPostIdOrderedByOldest() {
        Member member = new Member(
                null, "commenter2", "test1234!", "댓글러", null, null);
        Long memberId = memberRepository.save(member).getId();

        Post post = new Post(null, memberId, "제목", "내용", null);
        Long postId = postRepository.save(post).getId();

        Long firstCommentId = commentRepository.save(
                new Comment(null, postId, memberId, "첫 댓글")).getId();
        Long secondCommentId = commentRepository.save(
                new Comment(null, postId, memberId, "두번째 댓글")).getId();
        Long thirdCommentId = commentRepository.save(
                new Comment(null, postId, memberId, "세번째 댓글")).getId();

        List<Comment> comments = commentRepository.findByPostId(postId);

        // 같은 트랜잭션 내에서 지정되어 created_at이 동일할 수 있으므로,
        // comment_id 타이브레이커가 없다면 이 순서가 보장되지 않는다
        assertThat(comments.get(0).getId()).isEqualTo(firstCommentId);
        assertThat(comments.get(1).getId()).isEqualTo(secondCommentId);
        assertThat(comments.get(2).getId()).isEqualTo(thirdCommentId);
    }
}