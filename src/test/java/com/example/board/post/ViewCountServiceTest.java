package com.example.board.post;

import com.example.board.common.RedisKeys;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;

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
    void sameMemberCountedOne() {
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
}