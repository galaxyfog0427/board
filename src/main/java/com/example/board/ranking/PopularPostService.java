package com.example.board.ranking;

import com.example.board.common.RedisKeys;
import com.example.board.post.PostListItem;
import com.example.board.post.PostRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PopularPostService {

    static final int TOP = 10;
    private static final int CANDIDATES = 15;

    private final StringRedisTemplate redisTemplate;
    private final PostRepository postRepository;

    public PopularPostService(StringRedisTemplate redisTemplate, PostRepository postRepository) {
        this.redisTemplate = redisTemplate;
        this.postRepository = postRepository;
    }

    public List<PopularPost> getTop() {
        Set<TypedTuple<String>> ranked;

        try {
            ranked = redisTemplate.opsForZSet()
                    .reverseRangeByScoreWithScores(RedisKeys.weeklyLikeRanking(),
                            1.0,
                            Double.POSITIVE_INFINITY,
                            0,
                            CANDIDATES);
        } catch (DataAccessException e) {
            log.warn("[인기글 조회 실패] cause={}", e.getMostSpecificCause().getMessage());
            return List.of();
        }

        if (ranked == null || ranked.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = ranked.stream()
                .map(tuple -> Long.parseLong(tuple.getValue()))
                .toList();

        Map<Long, PostListItem> postsById = postRepository.findListItemsByIds(postIds).stream()
                .collect(Collectors.toMap(PostListItem::getId, Function.identity()));

        return ranked.stream()
                .filter(tuple -> postsById.containsKey(Long.parseLong(tuple.getValue())))
                .limit(TOP)
                .map(tuple -> {
                    Long postId = Long.parseLong(tuple.getValue());
                    PostListItem item = postsById.get(postId);
                    return new PopularPost(item.getId(), item.getTitle(), item.getWriterNickname(), tuple.getScore().longValue());
                })
                .toList();
    }
}
