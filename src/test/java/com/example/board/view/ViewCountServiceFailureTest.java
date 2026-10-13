package com.example.board.view;

import com.example.board.post.dto.PostListItem;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.data.redis.core.ZSetOperations;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
public class ViewCountServiceFailureTest {

    private static final RedisConnectionFailureException REDIS_DOWN =
            new RedisConnectionFailureException("Redis down");

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ZSetOperations<String, String> zSetOps;

    @Mock
    ValueOperations<String, String> valueOps;

    @InjectMocks
    ViewCountService viewCountService;

    @Test
    @DisplayName("Redis 장애 시 상세 화면의 대기 조회수는 0으로 응답하고 예외를 던지지 않는다")
    void getPendingCountWhenRedisDown() {
        given(redisTemplate.opsForZSet()).willReturn(zSetOps);
        given(zSetOps.score(anyString(), anyString())).willThrow(REDIS_DOWN);

        long pending = viewCountService.getPendingCount(1L);

        assertThat(pending).isZero();
    }

    @Test
    @DisplayName("Redis 장애 시 목록의 조회수는 DB 값 그대로 유지한다")
    void applyPendingViewWhenRedisDown() {
        PostListItem item = new PostListItem(1L, "title", "writer", 0, 10L, LocalDateTime.now());
        given(redisTemplate.opsForZSet()).willReturn(zSetOps);
        given(zSetOps.score(anyString(), any(Object[].class))).willThrow(REDIS_DOWN);

        viewCountService.applyPendingView(List.of(item));

        assertThat(item.getViewCount()).isEqualTo(10L);
    }

    @Test
    @DisplayName("Redis 장애 시 조회수 증가는 조용히 실패하고, 중복 확인이 실패했으니 증가 명령은 보내지 않는다")
    void increaseWhenRedisDown() {
        given(redisTemplate.opsForValue()).willReturn(valueOps);
        given(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class)))
                .willThrow(REDIS_DOWN);

        assertThatCode(() -> viewCountService.increaseForMember(1L, 7L))
                .doesNotThrowAnyException();

        then(redisTemplate).should(never()).opsForZSet();
    }

}
