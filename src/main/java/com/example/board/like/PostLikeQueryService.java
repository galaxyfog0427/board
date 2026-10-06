package com.example.board.like;

import com.example.board.global.redis.RedisKeys;
import com.example.board.post.dto.PostListItem;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PostLikeQueryService {

    static final String SENTINEL = "_";
    private static final String ANONYMOUS = "0";
    private static final Duration CACHE_TTL = Duration.ofHours(1);

    @SuppressWarnings("rawtypes")
    private static final RedisScript<List> STATUS_SCRIPT =
            RedisScript.of(new ClassPathResource("scripts/like_status.lua"), List.class);
    private static final RedisScript<Long> REBUILD_SCRIPT =
            RedisScript.of(new ClassPathResource("scripts/rebuild_likes.lua"), Long.class);

    private final StringRedisTemplate redisTemplate;
    private final PostLikeRepository postLikeRepository;

    public PostLikeQueryService(StringRedisTemplate redisTemplate, PostLikeRepository postLikeRepository) {
        this.redisTemplate = redisTemplate;
        this.postLikeRepository = postLikeRepository;
    }

    public LikeStatus getStatus(Long postId, Long memberId) {
        String key = RedisKeys.postLikes(postId);
        String viewer = memberId == null ? ANONYMOUS : String.valueOf(memberId);

        List<?> cached;
        try {
            cached = redisTemplate.execute(STATUS_SCRIPT, List.of(key), viewer);
        } catch (DataAccessException e) {
            log.warn("[좋아요 캐시 조회 실패] postId={}, cause={}", postId, e.getMostSpecificCause().getMessage());
            return readFromDb(postId, memberId);
        }

        if (cached != null && !cached.isEmpty()) {
            long count = ((Number) cached.get(0)).longValue();
            boolean likedByMe = ((Number) cached.get(1)).longValue() == 1L;
            return new LikeStatus(count, likedByMe);
        }

        List<Long> memberIds = postLikeRepository.findMemberIdsByPostId(postId);
        rebuild(key, memberIds);
        return new LikeStatus(memberIds.size(), memberId != null && memberIds.contains(memberId));
    }

    public void applyLikeCounts(List<PostListItem> items) {
        if (items.isEmpty()) {
            return;
        }
        List<Long> postIds = items.stream().map(PostListItem::getId).toList();
        Map<Long, Long> counts = postLikeRepository.countGroupByPostIds(postIds).stream()
                .collect(Collectors.toMap(PostLikeCount::postId, PostLikeCount::likeCount));
        items.forEach(item -> item.applyLikeCount(counts.getOrDefault(item.getId(), 0L)));
    }

    private void rebuild(String key, List<Long> memberIds) {
        Object[] args = new Object[memberIds.size() + 2];
        args[0] = String.valueOf(CACHE_TTL.toSeconds());
        args[1] = SENTINEL;
        for (int i = 0; i < memberIds.size(); i++) {
            args[i + 2] = String.valueOf(memberIds.get(i));
        }
        try {
            redisTemplate.execute(REBUILD_SCRIPT, List.of(key), args);
        } catch (DataAccessException e) {
            log.warn("[좋아요 캐시 재구성 실패] key={}, cause={}", key, e.getMostSpecificCause().getMessage());
        }
    }

    private LikeStatus readFromDb(Long postId, Long memberId) {
        long count = postLikeRepository.countByPostId(postId);
        boolean likedByMe = memberId != null && postLikeRepository.existsByPostIdAndMemberId(postId, memberId);
        return new LikeStatus(count, likedByMe);
    }
}
