package com.example.board.comment;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CommentRepositoryTest {

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    CommentRepository commentRepository;

    @Test
    void crud() throws SQLException {
        Member member = new Member(
                null,
                "commenter",
                "member1234!",
                "댓글러",
                null,
                null,
                null,
                null);
        Long memberId = memberRepository.save(member);

        Post post = new Post(
                null,
                memberId,
                "제목",
                "내용",
                null,
                null
        );
        Long postId = postRepository.save(post);

        Comment comment = new Comment(
                null,
                postId,
                memberId,
                "댓글",
                null,
                null
        );
        Long commentId = commentRepository.save(comment);

        Comment foundComment = commentRepository.findById(commentId);
        assertThat(foundComment.getContent()).isEqualTo(comment.getContent());
        assertThat(foundComment.getPostId()).isEqualTo(comment.getPostId());
        assertThat(foundComment.getMemberId()).isEqualTo(comment.getMemberId());
    }

}