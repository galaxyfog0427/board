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

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class CommentServiceTest {

    @Autowired
    CommentService commentService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("댓글 작성 성공시 카운트 증가")
    void commentCompleted() {
        Member savedMember = memberRepository.save(new Member(null, "writer1", "writer1!", "작성자1", null, null));
        Post savedPost = postRepository.save(new Post(null, savedMember, "제목", "내용", null));

        commentService.addComment(new Comment(null, savedPost, savedMember, "정상 댓글"));

        Post foundPost = postRepository.findById(savedPost.getId()).get();
        assertThat(foundPost.getCommentCount()).isOne();
    }

    @Test
    @DisplayName("댓글 작성 실패시 전체 롤백")
    void commentFailed() {
        Member savedMember = memberRepository.save(new Member(null, "writer2", "writer2!", "작성자2", null, null));
        Post savedPost = postRepository.save(new Post(null, savedMember, "제목", "내용", null));

        assertThatThrownBy(() ->
                commentService.addComment(new Comment(null, savedPost, savedMember, "ROLLBACK_TEST"))
        ).isInstanceOf(IllegalStateException.class);

        Post foundPost = postRepository.findById(savedPost.getId()).get();
        assertThat(foundPost.getCommentCount()).isZero();
    }
}