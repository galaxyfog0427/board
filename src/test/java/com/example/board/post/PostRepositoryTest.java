package com.example.board.post;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class PostRepositoryTest {

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void crud() {
        Member member = new Member(null, "postTester", "test1234!", "테스터", null, null, null, null);
        Long memberId = memberRepository.save(member);

        //create
        Post post = new Post(
                null,
                memberId,
                "JDBC 게시글",
                "순수 JDBC로 저장",
                null,
                null,
                null
        );
        Long savedPostId = postRepository.save(post);

        //read
        Post foundPost = postRepository.findById(savedPostId);
        assertThat(foundPost.getTitle()).isEqualTo(post.getTitle());
        assertThat(foundPost.getContent()).isEqualTo(post.getContent());

        //update
        postRepository.update(savedPostId, "수정된 제목", "수정된 내용");
        Post updatedPost = postRepository.findById(savedPostId);
        System.out.println(updatedPost);

        //delete
        postRepository.delete(savedPostId);
        assertThatThrownBy(() -> postRepository.findById(savedPostId))
                .isInstanceOf(EmptyResultDataAccessException.class);
    }

    @Test
    @DisplayName("findAll()은 최신 게시글이 먼저 오도록 정렬한다")
    void findAllOrderedByLatest() {
        Member member = new Member(null, "listTester", "test1234!", "리스트테스트", null, null, null, null);
        Long memberId = memberRepository.save(member);

        Long firstPostId = postRepository.save(
                new Post(null, memberId, "첫 번째 글", "내용1", null, null, null));
        Long secondPostId = postRepository.save(
                new Post(null, memberId, "두 번째 글", "내용2", null, null, null));
        Long thirdPostId = postRepository.save(
                new Post(null, memberId, "세 번째 글", "내용3", null, null, null));

        List<Post> posts = postRepository.findAll();
        assertThat(posts.get(0).getId()).isEqualTo(thirdPostId);
        assertThat(posts.get(1).getId()).isEqualTo(secondPostId);
        assertThat(posts.get(2).getId()).isEqualTo(firstPostId);
    }
}