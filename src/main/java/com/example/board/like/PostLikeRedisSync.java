package com.example.board.like;

import com.example.board.global.redis.RedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.time.Duration;
import java.util.List;

@Slf4j
@Component
public class PostLikeRedisSync {

    private static final RedisScript<Long> LIKE_SCRIPT =
            RedisScript.of(new ClassPathResource("scripts/like.lua"), Long.class);
    private static final RedisScript<Long> UNLIKE_SCRIPT =
            RedisScript.of(new ClassPathResource("scripts/unlike.lua"), Long.class);
    private static final String RANKING_TTL_SECONDS = String.valueOf(Duration.ofDays(8).toSeconds());

    private final StringRedisTemplate redisTemplate;

    public PostLikeRedisSync(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onLiked(PostLikedEvent event) {
        try {
            redisTemplate.execute(LIKE_SCRIPT,
                    List.of(RedisKeys.postLikes(event.postId()), RedisKeys.dailyLikeRanking(event.likedDate())),
                    String.valueOf(event.memberId()), String.valueOf(event.postId()), RANKING_TTL_SECONDS);
        } catch (DataAccessException e) {
            log.warn("[좋아요 Redis 반영 실패] postId={}, cause={}", event.postId(), e.getMostSpecificCause().getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onUnliked(PostUnlikedEvent event) {
        try {
            redisTemplate.execute(UNLIKE_SCRIPT,
                    List.of(RedisKeys.postLikes(event.postId()), RedisKeys.dailyLikeRanking(event.likedDate())),
                    String.valueOf(event.memberId()), String.valueOf(event.postId()));
        } catch (DataAccessException e) {
            log.warn("[좋아요 취소 Redis 반영 실패] postId={}, cause={}", event.postId(), e.getMostSpecificCause().getMessage());
        }
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onCacheStale(PostLikeCacheStaleEvent event) {
        try {
            redisTemplate.delete(RedisKeys.postLikes(event.postId()));
        } catch (DataAccessException e) {
            log.warn("[좋아요 캐시 삭제 실패] postId={}, cause={}", event.postId(), e.getMostSpecificCause().getMessage());
        }
    }
}
