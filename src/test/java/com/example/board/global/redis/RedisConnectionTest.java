package com.example.board.global.redis;

import com.example.board.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
public class RedisConnectionTest extends IntegrationTestSupport {

    private static final String TEST_KEY = "board:test:ping";

    @Autowired
    StringRedisTemplate redisTemplate;

    @AfterEach
    void cleanUp() {
        redisTemplate.delete(TEST_KEY);
    }

    @Test
    @DisplayName("SET 후 GET으로 같은 값을 읽고, 없는 키는 null을 반환한다")
    void setAndGet() {
        redisTemplate.opsForValue().set(TEST_KEY, "pong");

        assertThat(redisTemplate.opsForValue().get(TEST_KEY)).isEqualTo("pong");
        assertThat(redisTemplate.opsForValue().get("board:test:not-exists")).isNull();
    }
}
