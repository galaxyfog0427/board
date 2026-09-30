package com.example.board.like;

import com.example.board.post.PostNotFoundException;
import com.example.board.post.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final PostRepository postRepository;

    public PostLikeService(PostLikeRepository postLikeRepository, PostRepository postRepository) {
        this.postLikeRepository = postLikeRepository;
        this.postRepository = postRepository;
    }

    /**
     * @return 새로 좋아요가 추가되었으면 true, 이미 눌러져 있었으면 false
     */
    @Transactional
    public boolean like(Long postId, Long memberId) {
        validatePostExists(postId);
        int inserted = postLikeRepository.insertIgnore(postId, memberId);
        /**
         * TODO
         * inserted == 1 일 때만 커밋 후 Redis 반영 이벤트 발행
         */
        return inserted == 1;
    }

    /**
     * @return 실제로 좋아요가 취소되었으면 true, 원래 없었으면 false
     */
    @Transactional
    public boolean unlike(Long postId, Long memberId) {
        return postLikeRepository.findByPostIdAndMemberId(postId, memberId)
                .map(postLike -> postLikeRepository.deleteByIdReturningCount(postLike.getId()) == 1)
                /**
                 * TODO
                 * 삭제 성공 시 postLike.getCreatedAt()을 담아 이벤트 발행 (랭킹의 어느 날짜에서 뺄지)
                 */
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
