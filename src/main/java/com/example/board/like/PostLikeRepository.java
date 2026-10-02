package com.example.board.like;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    @Modifying
    @Query(value = "INSERT IGNORE INTO post_like (post_id, member_id, created_at) VALUES (:postId, :memberId, :createdAt)",
            nativeQuery = true)
    int insertIgnore(@Param("postId") Long postId,
                     @Param("memberId") Long memberId,
                     @Param("createdAt") LocalDateTime createdAt);

    @Modifying
    @Query("DELETE FROM PostLike pl WHERE pl.id = :id")
    int deleteByIdReturningCount(@Param("id") Long id);

    Optional<PostLike> findByPostIdAndMemberId(Long postId, Long memberId);

    long countByPostId(Long postId);

}
