package com.example.board.common;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class RedisConnectionTest {

    private static final String TEST_KEY = "board:test:ping";

    @Autowired
    StringRedisTemplate redisTemplate;

    @AfterEach
    void cleanUp() {
        redisTemplate.delete(TEST_KEY);
    }

    @Test
    @DisplayName("테스트는 1번 DB를 사용한다")
    void usesTestDatabase() {
        LettuceConnectionFactory factory = (LettuceConnectionFactory) redisTemplate.getConnectionFactory();
        assertThat(factory.getDatabase()).isEqualTo(1);
    }

    @Test
    @DisplayName("SET 후 GET으로 같은 값을 읽고, 없는 키는 null을 반환한다")
    void setAndGet() {
        redisTemplate.opsForValue().set(TEST_KEY, "pong");

        assertThat(redisTemplate.opsForValue().get(TEST_KEY)).isEqualTo("pong");
        assertThat(redisTemplate.opsForValue().get("board:test:not-exists")).isNull();
    }
}
