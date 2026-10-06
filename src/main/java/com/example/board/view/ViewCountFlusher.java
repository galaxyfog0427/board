package com.example.board.post;

import com.example.board.global.redis.RedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.RedisCallback;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.support.TransactionTemplate;

import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ViewCountFlusher {

    private static final int BATCH_SIZE = 1000;
    private static final String UPDATE_SQL = "UPDATE post SET view_count = view_count + ? WHERE post_id = ?";

    private final StringRedisTemplate redisTemplate;
    private final JdbcTemplate jdbcTemplate;
    private final TransactionTemplate transactionTemplate;

    public ViewCountFlusher(StringRedisTemplate redisTemplate,
                            JdbcTemplate jdbcTemplate,
                            TransactionTemplate transactionTemplate) {
        this.redisTemplate = redisTemplate;
        this.jdbcTemplate = jdbcTemplate;
        this.transactionTemplate = transactionTemplate;
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void scheduledFlush() {
        int flushed = flush();
        if (flushed > 0) {
            log.info("[조회수 반영] {}건", flushed);
        }
    }

    public int flush() {
        String key = RedisKeys.pendingViews();

        // 1. 읽기: ZRANGE key 1 +inf BYSCORE LIMIT 0 1000 WITHSCORES
        Set<TypedTuple<String>> entries;
        try {
            entries = redisTemplate.opsForZSet()
                    .rangeByScoreWithScores(key, 1, Double.POSITIVE_INFINITY, 0, BATCH_SIZE);
        } catch (DataAccessException e) {
            log.warn("[조회수 반영 건너뜀] cause={}", e.getMostSpecificCause().getMessage());
            return 0;
        }
        if (entries == null || entries.isEmpty()) {
            return 0;
        }

        // 2. DB 반영: 전부 성공하거나 전부 롤백
        List<Object[]> params = entries.stream()
                .map(entry -> new Object[]{entry.getScore().longValue(), Long.parseLong(entry.getValue())})
                .toList();
        transactionTemplate.executeWithoutResult(status -> jdbcTemplate.batchUpdate(UPDATE_SQL, params));

        // 3~4. 커밋 이후에만 Redis에서 차감하고 0점 멤버 청소 (한 번의 왕복으로)
        RedisSerializer<String> serializer = redisTemplate.getStringSerializer();
        byte[] rawKey = serializer.serialize(key);
        try {
            redisTemplate.executePipelined((RedisCallback<Object>) connection -> {
                for (TypedTuple<String> entry : entries) {
                    connection.zSetCommands().zIncrBy(rawKey, -entry.getScore(), serializer.serialize(entry.getValue()));
                }
                connection.zSetCommands().zRemRangeByScore(rawKey, Double.NEGATIVE_INFINITY, 0);
                return null;
            });
        } catch (DataAccessException e) {
            log.error("[조회수 차감 실패] DB 반영 후 Redis 차감 실패, 다음 반영 시 중복 가산 가능. {}건, cause={}",
                    entries.size(), e.getMostSpecificCause().getMessage());
        }
        return entries.size();
    }
}
