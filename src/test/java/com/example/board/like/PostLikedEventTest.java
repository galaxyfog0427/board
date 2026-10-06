package com.example.board.like;

import com.example.board.global.redis.RedisKeys;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.domain.Post;
import com.example.board.post.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.support.TransactionTemplate;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class PostLikedEventTest {

    @Autowired
    PostLikeService postLikeService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    TransactionTemplate transactionTemplate;

    Long postId;
    Long memberId;
    String todayKey = RedisKeys.dailyLikeRanking(LocalDate.now());

    @BeforeEach
    void setUp() {
        Member member = memberRepository.save(
                new Member(
                        null,
                        "eventTester",
                        "test1234!",
                        "이벤트테스터",
                        null,
                        null
                )
        );
        memberId = member.getId();
        postId = postRepository.save(
                new Post(
                        null,
                        member,
                        "title",
                        "content",
                        null
                )
        ).getId();
    }

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM post_like WHERE post_id = ?", postId);
        postRepository.deleteById(postId);
        memberRepository.deleteById(memberId);
        redisTemplate.opsForZSet().remove(todayKey, String.valueOf(postId));
        redisTemplate.delete(RedisKeys.postLikes(postId));
    }

    @Test
    @DisplayName("좋아요가 커밋되면 오늘 랭킹에 반영된다")
    void committedLikeIsReflected() {
        postLikeService.like(postId, memberId);

        assertThat(redisTemplate.opsForZSet().score(todayKey, String.valueOf(postId))).isEqualTo(1.0);
    }

    @Test
    @DisplayName("좋아요 트랜잭션이 롤백되면 Redis에는 아무것도 반영되지 않는다")
    void rolledBackLikeIsNotReflected() {
        transactionTemplate.executeWithoutResult(status -> {
            postLikeService.like(postId, memberId);
            status.setRollbackOnly();
        });

        assertThat(redisTemplate.opsForZSet().score(todayKey, String.valueOf(postId))).isNull();
    }

    @Test
    @DisplayName("이미 누른 좋아요를 다시 누르면 캐시가 틀렸다고 보고 Set을 삭제한다")
    void staleCacheIsEvictedOnDuplicateLike() {
        postLikeService.like(postId, memberId);
        redisTemplate.opsForSet().add(RedisKeys.postLikes(postId), PostLikeQueryService.SENTINEL);

        boolean liked = postLikeService.like(postId, memberId);

        assertThat(liked).isFalse();
        assertThat(redisTemplate.hasKey(RedisKeys.postLikes(postId))).isFalse();
    }

}