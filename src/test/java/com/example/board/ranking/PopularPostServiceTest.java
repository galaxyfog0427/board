package com.example.board.ranking;

import com.example.board.common.RedisKeys;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class PopularPostServiceTest {

    @Autowired
    PopularPostService popularPostService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    StringRedisTemplate redisTemplate;

    @AfterEach
    void cleanUp() {
        redisTemplate.delete(RedisKeys.weeklyLikeRanking());
    }

    @Test
    @DisplayName("점수 높은 순으로, DB에 있고 점수가 1 이상인 글만 보여 준다")
    void topPosts() {
        Member writer = memberRepository.save(new Member(
                null, "rankWriter", "test1234!", "랭커", null, null
        ));
        Long first = postRepository.save(new Post(null, writer, "1등 글", "c", null)).getId();
        Long second = postRepository.save(new Post(null, writer, "2등 글", "c", null)).getId();
        Long zero = postRepository.save(new Post(null, writer, "0점 글", "c", null)).getId();

        setWeekly(first, 5);
        setWeekly(second, 2);
        setWeekly(zero, 0);
        setWeekly(999999999L, 10);

        List<PopularPost> result = popularPostService.getTop();

        assertThat(result).extracting(PopularPost::postId).containsExactly(first, second);
        assertThat(result.get(0).likeCount()).isEqualTo(5L);
        assertThat(result.get(0).writerNickname()).isEqualTo("랭커");
    }

    @Test
    @DisplayName("랭킹이 비어 있으면 빈 리스트")
    void emptyRanking() {
        assertThat(popularPostService.getTop()).isEmpty();
    }

    private void setWeekly(Long postId, double score) {
        redisTemplate.opsForZSet().add(RedisKeys.weeklyLikeRanking(), String.valueOf(postId), score);
    }

}