package com.example.board.like;

import com.example.board.post.PostNotFoundException;
import com.example.board.post.PostRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;
    private final ApplicationEventPublisher eventPublisher;

    public PostLikeService(PostLikeRepository postLikeRepository,
                           PostRepository postRepository,
                           ApplicationEventPublisher eventPublisher) {
        this.postLikeRepository = postLikeRepository;
        this.postRepository = postRepository;
        this.eventPublisher = eventPublisher;
    }

    /**
     * @return 새로 좋아요가 추가되었으면 true, 이미 눌러져 있었으면 false
     */
    @Transactional
    public boolean like(Long postId, Long memberId) {
        validatePostExists(postId);
        LocalDateTime now = LocalDateTime.now().truncatedTo(ChronoUnit.SECONDS);
        boolean liked = postLikeRepository.insertIgnore(postId, memberId, now) == 1;
        if (liked) {
            eventPublisher.publishEvent(new PostLikedEvent(postId, memberId, now.toLocalDate()));
        }
        return liked;
    }

    /**
     * @return 실제로 좋아요가 취소되었으면 true, 원래 없었으면 false
     */
    @Transactional
    public boolean unlike(Long postId, Long memberId) {
        return postLikeRepository.findByPostIdAndMemberId(postId, memberId)
                .map(postLike -> {
                    boolean unliked = postLikeRepository.deleteByIdReturningCount(postLike.getId()) == 1;
                    if (unliked) {
                        eventPublisher.publishEvent(
                                new PostUnlikedEvent(postId, memberId, postLike.getCreatedAt().toLocalDate()));
                    }
                    return unliked;
                })
                .orElse(false);
    }

    @Transactional(readOnly = true)
    public long countLike(Long postId) {
        return postLikeRepository.countByPostId(postId);
    }

    private void validatePostExists(Long postId) {
        if (!postRepository.existsById(postId)) {
            throw new PostNotFoundException("존재하지 않는 게시글입니다.");
        }
    }
}
