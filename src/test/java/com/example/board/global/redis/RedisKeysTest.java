package com.example.board.global.redis;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.assertj.core.api.Assertions.*;

class RedisKeysTest {

    @Test
    @DisplayName("모든 키는 board: prefix로 시작하고 설계 문서의 형식을 따른다")
    void keyFormats() {
        assertThat(RedisKeys.postLikes(1L)).isEqualTo("board:post:1:likes");
        assertThat(RedisKeys.dailyLikeRanking(LocalDate.of(2026, 9, 8)))
                .isEqualTo("board:ranking:likes:20260908");
        assertThat(RedisKeys.weeklyLikeRanking()).isEqualTo("board:ranking:likes:weekly");
        assertThat(RedisKeys.pendingViews()).isEqualTo("board:views:pending");
        assertThat(RedisKeys.viewDedupForMember(1L, 7L))
                .isEqualTo("board:view:post:1:member:7");
        assertThat(RedisKeys.viewDedupForVisitor(1L, "abc-123"))
                .isEqualTo("board:view:post:1:visitor:abc-123");
    }

}