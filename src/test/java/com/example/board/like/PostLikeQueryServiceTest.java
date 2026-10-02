package com.example.board.like;


import com.example.board.common.RedisKeys;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.Post;
import com.example.board.post.PostListItem;
import com.example.board.post.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class PostLikeQueryServiceTest {

    @Autowired
    PostLikeQueryService postLikeQueryService;

    @Autowired
    PostLikeService postLikeService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    StringRedisTemplate redisTemplate;

    List<String> keysToDelete = new ArrayList<>();

    @AfterEach
    void cleanUp() {
        redisTemplate.delete(keysToDelete);
    }

    @Test
    @DisplayName("캐시가 없으면 DB로 재구성하고, 센티넬과 TTL을 건다")
    void rebuildWhenNotCached() {
        Member member = saveMember("queryLiker1");
        Long postId = savePost(member);
        postLikeService.like(postId, member.getId());

        LikeStatus status = postLikeQueryService.getStatus(postId, member.getId());
        LikeStatus cachedStatus = postLikeQueryService.getStatus(postId, member.getId());

        assertThat(cachedStatus).isEqualTo(status);
        assertThat(status.count()).isOne();
        assertThat(status.likedByMe()).isTrue();
        assertThat(redisTemplate.opsForSet().members(RedisKeys.postLikes(postId)))
                .containsExactlyInAnyOrder(PostLikeQueryService.SENTINEL, String.valueOf(member.getId()));
        assertThat(redisTemplate.getExpire(RedisKeys.postLikes(postId))).isBetween(1L, 3600L);
    }

    @Test
    @DisplayName("좋아요가 0개인 글도 센티넬로 캐시된다")
    void zeroLikesAreCached() {
        Member member = saveMember("queryLiker2");
        Long postId = savePost(member);

        LikeStatus status = postLikeQueryService.getStatus(postId, member.getId());
        LikeStatus cachedStatus = postLikeQueryService.getStatus(postId, member.getId());

        assertThat(cachedStatus).isEqualTo(status);
        assertThat(status.count()).isZero();
        assertThat(status.likedByMe()).isFalse();
        assertThat(redisTemplate.opsForSet().size(RedisKeys.postLikes(postId))).isOne();
    }

    @Test
    @DisplayName("캐시가 있으면 DB를 보지 않고 캐시로 답한다")
    void readFromCache() {
        Member member = saveMember("queryLiker3");
        Long postId = savePost(member);
        redisTemplate.opsForSet().add(RedisKeys.postLikes(postId), PostLikeQueryService.SENTINEL, "5", "6", "7");

        LikeStatus status = postLikeQueryService.getStatus(postId, 6L);

        assertThat(status.count()).isEqualTo(3L);
        assertThat(status.likedByMe()).isTrue();
    }

    @Test
    @DisplayName("비회원은 항상 likeByMe가 false다")
    void anonymousNeverLiked() {
        Member member = saveMember("queryLiker4");
        Long postId = savePost(member);
        postLikeService.like(postId, member.getId());

        LikeStatus status = postLikeQueryService.getStatus(postId, null);

        assertThat(status.count()).isOne();
        assertThat(status.likedByMe()).isFalse();
    }

    @Test
    @DisplayName("목록의 각 글에 좋아요 수를 한 번의 쿼리로 채운다")
    void applyLikeCounts() {
        Member writer = saveMember("queryLiker5");
        Member other = saveMember("queryLiker6");
        Long likedPostId = savePost(writer);
        Long notLikedPostId = savePost(writer);
        postLikeService.like(likedPostId, writer.getId());
        postLikeService.like(likedPostId, other.getId());

        PostListItem liked = new PostListItem(likedPostId, "t", "w", 0, 0L, LocalDateTime.now());
        PostListItem notLiked = new PostListItem(notLikedPostId, "t", "w", 0, 0L, LocalDateTime.now());

        postLikeQueryService.applyLikeCounts(List.of(liked, notLiked));

        assertThat(liked.getLikeCount()).isEqualTo(2L);
        assertThat(notLiked.getLikeCount()).isZero();
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