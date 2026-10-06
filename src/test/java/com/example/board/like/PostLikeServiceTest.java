package com.example.board.like;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.domain.Post;
import com.example.board.post.exception.PostNotFoundException;
import com.example.board.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class PostLikeServiceTest {

    @Autowired
    PostLikeService postLikeService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("좋아요는 한 번만 반영되고, 두 번째 요청은 false를 반환한다")
    void likeIsIdempotent() {
        Member member = saveMember("liker1");
        Long postId = savePost(member);

        assertThat(postLikeService.like(postId, member.getId())).isTrue();
        assertThat(postLikeService.like(postId, member.getId())).isFalse();
        assertThat(postLikeService.countLike(postId)).isOne();
    }

    @Test
    @DisplayName("좋아요 취소도 한 번만 반영되고, 두 번째 요청은 false를 반환한다")
    void unlikeIsIdempotent() {
        Member member = saveMember("liker2");
        Long postId = savePost(member);
        postLikeService.like(postId, member.getId());

        assertThat(postLikeService.unlike(postId, member.getId())).isTrue();
        assertThat(postLikeService.unlike(postId, member.getId())).isFalse();
        assertThat(postLikeService.countLike(postId)).isZero();
    }

    @Test
    @DisplayName("존재하지 않는 게시글에 좋아요를 누르면 PostNotFoundException")
    void likeNotFoundPost() {
        Member member = saveMember("liker3");

        assertThatThrownBy(() -> postLikeService.like(9999999999L, member.getId()))
                .isInstanceOf(PostNotFoundException.class);
    }

    private Member saveMember(String loginId) {
        return memberRepository.save(
                new Member(
                        null,
                        loginId,
                        "test1234!",
                        loginId,
                        null,
                        null
                )
        );
    }

    private Long savePost(Member writer) {
        return postRepository.save(
                new Post(
                        null,
                        writer,
                        "title",
                        "content",
                        null
                )
        ).getId();
    }
}