package com.example.board.ranking;

import com.example.board.common.RedisKeys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class RankingAggregator {

    static final int DAYS = 7;
    private static final Duration WEEKLY_TTL = Duration.ofMinutes(5);

    private final StringRedisTemplate redisTemplate;

    public RankingAggregator(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Scheduled(fixedDelay = 1, timeUnit = TimeUnit.MINUTES)
    public void scheduledAggregate() {
        aggregate(LocalDate.now());
    }

    public void aggregate(LocalDate today) {
        String weeklyKey = RedisKeys.weeklyLikeRanking();

        List<String> dailyKeys = new ArrayList<>();

        for (int i = 0; i < DAYS; i++) {
            dailyKeys.add(RedisKeys.dailyLikeRanking(today.minusDays(i)));
        }

        try {
            String firstKey = dailyKeys.get(0);
            List<String> otherKeys = dailyKeys.subList(1, dailyKeys.size());
            redisTemplate.opsForZSet().unionAndStore(firstKey, otherKeys, weeklyKey);
            redisTemplate.expire(weeklyKey, WEEKLY_TTL);
        } catch (DataAccessException e) {
            log.warn("[일별 좋아요 랭킹 주간으로 합치기 실패] cause={}", e.getMostSpecificCause().getMessage());
        }
    }
}
