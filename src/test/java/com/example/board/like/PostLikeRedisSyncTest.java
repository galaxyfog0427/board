package com.example.board.like;

import com.example.board.common.RedisKeys;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.Duration;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class PostLikeRedisSyncTest {

    private static final Long POST_ID = 1L;
    private static final Long MEMBER_ID = 7L;
    private static final LocalDate TODAY = LocalDate.now();
    private static final LocalDate OLD_DAY = TODAY.minusDays(10);

    @Autowired
    PostLikeRedisSync postLikeRedisSync;

    @Autowired
    StringRedisTemplate redisTemplate;

    @AfterEach
    void cleanUp() {
        redisTemplate.delete(List.of(
                RedisKeys.postLikes(POST_ID),
                RedisKeys.dailyLikeRanking(TODAY),
                RedisKeys.dailyLikeRanking(OLD_DAY)
        ));
    }

    @Test
    @DisplayName("Set이 캐시되어 있지 않으면 만들지 않고, 오늘 랭킹만 +1 하며 TTL을 건다")
    void likeWithoutCachedSet() {
        postLikeRedisSync.onLiked(new PostLikedEvent(POST_ID, MEMBER_ID, TODAY));

        assertThat(redisTemplate.hasKey(RedisKeys.postLikes(POST_ID))).isFalse();
        assertThat(redisTemplate.opsForZSet().score(RedisKeys.dailyLikeRanking(TODAY), "1")).isEqualTo(1.0);
        assertThat(redisTemplate.getExpire(RedisKeys.dailyLikeRanking(TODAY)))
                .isBetween(Duration.ofDays(7).toSeconds(), Duration.ofDays(8).toSeconds());
    }

    @Test
    @DisplayName("Set이 캐시되어 있으면 회원을 추가한다")
    void likeWithCachedSet() {
        redisTemplate.opsForSet().add(RedisKeys.postLikes(POST_ID), "3");

        postLikeRedisSync.onLiked(new PostLikedEvent(POST_ID, MEMBER_ID, TODAY));

        assertThat(redisTemplate.opsForSet().isMember(RedisKeys.postLikes(POST_ID), "7")).isTrue();
        assertThat(redisTemplate.opsForSet().size(RedisKeys.postLikes(POST_ID))).isEqualTo(2L);
    }

    @Test
    @DisplayName("취소하면 Set에서 빠지고 좋아요를 눌렀던 날의 랭킹이 -1 된다")
    void unlike() {
        redisTemplate.opsForSet().add(RedisKeys.postLikes(POST_ID), "7");
        redisTemplate.opsForZSet().incrementScore(RedisKeys.dailyLikeRanking(TODAY), "1", 2);

        postLikeRedisSync.onUnliked(new PostUnlikedEvent(POST_ID, MEMBER_ID, TODAY));

        assertThat(redisTemplate.opsForSet().isMember(RedisKeys.postLikes(POST_ID), "7")).isFalse();
        assertThat(redisTemplate.opsForZSet().score(RedisKeys.dailyLikeRanking(TODAY), "1")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("이미 만료된 과거 랭킹 키는 취소 때문에 되살아나지 않는다")
    void unlikeOldLikeDoesNotRecreateKey() {
        postLikeRedisSync.onUnliked(new PostUnlikedEvent(POST_ID, MEMBER_ID, OLD_DAY));

        assertThat(redisTemplate.hasKey(RedisKeys.dailyLikeRanking(OLD_DAY))).isFalse();
    }
}