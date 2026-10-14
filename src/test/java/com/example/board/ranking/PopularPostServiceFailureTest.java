package com.example.board.ranking;

import com.example.board.post.repository.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.then;

@ExtendWith(MockitoExtension.class)
public class PopularPostServiceFailureTest {

    private static final RedisConnectionFailureException REDIS_DOWN
            = new RedisConnectionFailureException("Redis down");

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    ZSetOperations<String, String> zSetOps;

    @Mock
    PostRepository postRepository;

    @InjectMocks
    PopularPostService popularPostService;

    @Test
    @DisplayName("Redis 장애 시 인기글은 빈 목록이고, DB는 조회하지 않는다")
    void getTopReturnsEmptyWhenRedisDown() {
        given(redisTemplate.opsForZSet()).willReturn(zSetOps);
        given(zSetOps.reverseRangeByScoreWithScores(anyString(), anyDouble(), anyDouble(), anyLong(), anyLong()))
                .willThrow(REDIS_DOWN);

        List<PopularPost> top = popularPostService.getTop();

        assertThat(top).isEmpty();
        then(postRepository).shouldHaveNoInteractions();
    }

}
