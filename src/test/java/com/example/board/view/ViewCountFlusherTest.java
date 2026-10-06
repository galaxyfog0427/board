package com.example.board.post;

import com.example.board.global.redis.RedisKeys;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.domain.Post;
import com.example.board.post.repository.PostRepository;
import com.example.board.view.ViewCountFlusher;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Transactional
class ViewCountFlusherTest {

    @Autowired
    ViewCountFlusher viewCountFlusher;

    @Autowired
    StringRedisTemplate redisTemplate;

    @Autowired
    JdbcTemplate jdbcTemplate;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @AfterEach
    void cleanUp() {
        redisTemplate.delete(RedisKeys.pendingViews());
    }

    @Test
    @DisplayName("대기 중인 조회수를 DB에 더하고 Redis에서는 제거한다")
    void flushAddsToDbAndClearRedis() {
        Long postId = savePost();
        redisTemplate.opsForZSet().incrementScore(RedisKeys.pendingViews(), String.valueOf(postId), 3);

        int flushed = viewCountFlusher.flush();

        assertThat(flushed).isOne();
        assertThat(viewCountInDb(postId)).isEqualTo(3L);
        assertThat(redisTemplate.opsForZSet().score(RedisKeys.pendingViews(), String.valueOf(postId))).isNull();
    }

    @Test
    @DisplayName("두 번 반영해도 중복 가산되지 않는다")
    void flushTwiceDoesNotDoubleCount() {
        Long postId = savePost();
        redisTemplate.opsForZSet().incrementScore(RedisKeys.pendingViews(), String.valueOf(postId), 3);

        viewCountFlusher.flush();
        viewCountFlusher.flush();

        assertThat(viewCountInDb(postId)).isEqualTo(3L);
    }

    @Test
    @DisplayName("존재하지 않는 게시글의 조회수는 에러 없이 청소된다")
    void nonExistsPostIsCleanedUp() {
        redisTemplate.opsForZSet().incrementScore(RedisKeys.pendingViews(), "9999999999", 5);

        viewCountFlusher.flush();

        assertThat(redisTemplate.opsForZSet().score(RedisKeys.pendingViews(), "9999999999")).isNull();
    }

    @Test
    @DisplayName("대기 중인 조회수가 없으면 아무것도 하지 않는다")
    void nothingToFlush() {
        assertThat(viewCountFlusher.flush()).isZero();
    }

    private Long savePost() {
        Member member = memberRepository.save(
                new Member(null, "flushTester", "test1234!", "반영테스터", null, null)
        );
        return postRepository.save(
                new Post(null, member, "title", "content", null)
        ).getId();
    }

    private Long viewCountInDb(Long postId) {
        return jdbcTemplate.queryForObject("SELECT view_count FROM post WHERE post_id = ?", Long.class, postId);
    }
}