package com.example.board.ranking;

import com.example.board.global.redis.RedisKeys;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class RankingAggregatorTest {

    private static final LocalDate TODAY = LocalDate.of(2026, 10, 4);

    @Autowired
    RankingAggregator rankingAggregator;

    @Autowired
    StringRedisTemplate redisTemplate;

    @AfterEach
    void cleanUp() {
        List<String> keys = new ArrayList<>();
        for (int i = 0; i <= RankingAggregator.DAYS; i++) {
            keys.add(RedisKeys.dailyLikeRanking(TODAY.minusDays(i)));
        }
        keys.add(RedisKeys.weeklyLikeRanking());
        redisTemplate.delete(keys);
    }

    @Test
    @DisplayName("최근 7일의 점수를 합산하고, 7일보다 오래된 날은 제외한다")
    void sumsLastSevenDays() {
        addScore(TODAY, "1", 2);
        addScore(TODAY.minusDays(6), "1", 3);
        addScore(TODAY.minusDays(7), "1", 100);
        addScore(TODAY, "2", 1);

        rankingAggregator.aggregate(TODAY);

        assertThat(weeklyScore("1")).isEqualTo(5.0);
        assertThat(weeklyScore("2")).isEqualTo(1.0);
    }

    @Test
    @DisplayName("결과 키에는 5분 이내의 수명이 걸린다")
    void weeklyKeyHasTtl() {
        addScore(TODAY, "1", 1);

        rankingAggregator.aggregate(TODAY);

        assertThat(redisTemplate.getExpire(RedisKeys.weeklyLikeRanking())).isBetween(1L, 300L);
    }

    @Test
    @DisplayName("여러 번 실행해도 결과가 같다 (멱등)")
    void idempotent() {
        addScore(TODAY, "1", 2);

        rankingAggregator.aggregate(TODAY);
        rankingAggregator.aggregate(TODAY);

        assertThat(weeklyScore("1")).isEqualTo(2.0);
    }

    private void addScore(LocalDate date, String postId, double score) {
        redisTemplate.opsForZSet().incrementScore(RedisKeys.dailyLikeRanking(date), postId, score);
    }

    private Double weeklyScore(String postId) {
        return redisTemplate.opsForZSet().score(RedisKeys.weeklyLikeRanking(), postId);
    }
}