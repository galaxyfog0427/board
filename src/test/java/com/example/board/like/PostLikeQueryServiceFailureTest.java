package com.example.board.like;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
public class PostLikeQueryServiceFailureTest {

    private static final RedisConnectionFailureException REDIS_DOWN
            = new RedisConnectionFailureException("Redis down");

    @Mock
    StringRedisTemplate redisTemplate;

    @Mock
    PostLikeRepository postLikeRepository;

    @InjectMocks
    PostLikeQueryService postLikeQueryService;

    @Test
    @DisplayName("Redis 장애 시 좋아요 수와 내 상태를 DB에서 읽어 응답한다")
    void getStatusFallsBackToDbWhenRedisDown() {
        given(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .willThrow(REDIS_DOWN);

        given(postLikeRepository.countByPostId(1L)).willReturn(3L);
        given(postLikeRepository.existsByPostIdAndMemberId(1L, 7L)).willReturn(true);

        LikeStatus status = postLikeQueryService.getStatus(1L, 7L);

        assertThat(status.count()).isEqualTo(3L);
        assertThat(status.likedByMe()).isTrue();

        then(postLikeRepository).should().countByPostId(1L);
        then(postLikeRepository).should().existsByPostIdAndMemberId(1L, 7L);
    }

    @Test
    @DisplayName("캐시가 비어 DB로 재구성하던 중 Redis가 죽어도 DB 결과로 정상 응답한다")
    void getStatusSurviveRebuildFailure() {
        given(redisTemplate.execute(any(RedisScript.class), anyList(), any(Object[].class)))
                .willReturn(List.of())
                .willThrow(REDIS_DOWN);

        given(postLikeRepository.findMemberIdsByPostId(1L)).willReturn(List.of(7L, 8L));

        LikeStatus status = postLikeQueryService.getStatus(1L, 7L);

        assertThat(status.count()).isEqualTo(2L);
        assertThat(status.likedByMe()).isTrue();
    }
}
