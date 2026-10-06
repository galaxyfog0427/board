package com.example.board.view;

import com.example.board.global.redis.RedisKeys;
import com.example.board.post.dto.PostListItem;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
class ViewCountServiceTest {

    private static final Long POST_ID = 1L;

    @Autowired
    ViewCountService viewCountService;

    @Autowired
    StringRedisTemplate redisTemplate;

    @AfterEach
    void cleanUp() {
        redisTemplate.delete(List.of(
                RedisKeys.pendingViews(),
                RedisKeys.viewDedupForMember(POST_ID, 7L),
                RedisKeys.viewDedupForMember(POST_ID, 8L),
                RedisKeys.viewDedupForVisitor(POST_ID, "visitor-a")
        ));
    }

    @Test
    @DisplayName("같은 회원이 여러 번 조회해도 한 번만 증가한다")
    void sameMemberCountedOnce() {
        viewCountService.increaseForMember(POST_ID, 7L);
        viewCountService.increaseForMember(POST_ID, 7L);
        viewCountService.increaseForMember(POST_ID, 7L);

        assertThat(viewCountService.getPendingCount(POST_ID)).isOne();
    }

    @Test
    @DisplayName("서로 다른 회원과 비회원은 각각 증가한다")
    void differentViewersCountedSeparately() {
        viewCountService.increaseForMember(POST_ID, 7L);
        viewCountService.increaseForMember(POST_ID, 8L);
        viewCountService.increaseForVisitor(POST_ID, "visitor-a");

        assertThat(viewCountService.getPendingCount(POST_ID)).isEqualTo(3L);
    }

    @Test
    @DisplayName("중복 방지 키는 10분 TTL을 가진다")
    void dedupKeyHasTtl() {
        viewCountService.increaseForMember(POST_ID, 7L);

        Long ttl = redisTemplate.getExpire(RedisKeys.viewDedupForMember(POST_ID, 7L));
        assertThat(ttl).isBetween(1L, 600L);
    }

    @Test
    @DisplayName("조회 기록이 없는 게시글의 대기 조회수는 0이다")
    void noPendingViews() {
        assertThat(viewCountService.getPendingCount(999L)).isZero();
    }

    @Test
    @DisplayName("목록의 각 게시글에 대기 중인 조회수를 한 번에 더한다")
    void applyPendingViews() {
        redisTemplate.opsForZSet().incrementScore(RedisKeys.pendingViews(), "1", 3);
        redisTemplate.opsForZSet().incrementScore(RedisKeys.pendingViews(), "3", 5);

        PostListItem item1 = new PostListItem(1L, "t1", "writer", 0, 10L, LocalDateTime.now());
        PostListItem item2 = new PostListItem(2L, "t2", "writer", 0, 7L, LocalDateTime.now());
        PostListItem item3 = new PostListItem(3L, "t3", "writer", 0, 0L, LocalDateTime.now());

        viewCountService.applyPendingView(List.of(item1, item2, item3));

        assertThat(item1.getViewCount()).isEqualTo(13L);
        assertThat(item2.getViewCount()).isEqualTo(7L);
        assertThat(item3.getViewCount()).isEqualTo(5L);
    }

    @Test
    @DisplayName("빈 목록이면 Redis를 호출하지 않고 끝난다")
    void applyPendingViewsToEmptyList() {
        viewCountService.applyPendingView(List.of());
    }
}