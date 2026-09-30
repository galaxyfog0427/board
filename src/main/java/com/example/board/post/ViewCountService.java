package com.example.board.post;

import com.example.board.common.RedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;

@Slf4j
@Service
public class ViewCountService {

    private static final Duration DEDUP_TTL = Duration.ofMinutes(10);

    private final StringRedisTemplate redisTemplate;

    public ViewCountService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public void increaseForMember(Long postId, Long memberId) {
        increase(postId, RedisKeys.viewDedupForMember(postId, memberId));
    }

    public void increaseForVisitor(Long postId, String visitorId) {
        increase(postId, RedisKeys.viewDedupForVisitor(postId, visitorId));
    }

    public long getPendingCount(Long postId) {
        try {
            Double score = redisTemplate.opsForZSet().score(RedisKeys.pendingViews(), String.valueOf(postId));
            return score == null ? 0L : score.longValue();
        } catch (DataAccessException e) {
            log.warn("[조회수 조회 실패] postId={}, cause={}", postId, e.getMostSpecificCause().getMessage());
            return 0L;
        }
    }

    public void applyPendingView(List<PostListItem> items) {
        if (items.isEmpty()) {
            return;
        }

        try {
            Object[] members = items.stream()
                    .map(item -> String.valueOf(item.getId()))
                    .toArray();
            List<Double> scores = redisTemplate.opsForZSet().score(RedisKeys.pendingViews(), members);

            for (int i = 0; i < items.size(); i++) {
                Double score = scores.get(i);
                if (score != null) {
                    items.get(i).addPendingViews(score.longValue());
                }
            }
        } catch (DataAccessException e) {
            log.warn("[목록 조회수 조회 실패] cause={}", e.getMostSpecificCause().getMessage());
        }
    }

    private void increase(Long postId, String dedupKey) {
        try {
            Boolean firstView = redisTemplate.opsForValue().setIfAbsent(dedupKey, "1", DEDUP_TTL);
            if (Boolean.TRUE.equals(firstView)) {
                redisTemplate.opsForZSet().incrementScore(RedisKeys.pendingViews(), String.valueOf(postId), 1);
            }
        } catch (DataAccessException e) {
            log.warn("[조회수 증가 실패] postId={}, cause={}", postId, e.getMostSpecificCause().getMessage());
        }
    }
}
