package com.example.board.like;

import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post_like")
public class PostLike {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_like_id")
    private Long id;

    private Long postId;
    private Long memberId;

    @Column(insertable = false, updatable = false)
    private LocalDateTime createdAt;

    protected PostLike() {
    }

    public Long getId() {
        return id;
    }

    public Long getPostId() {
        return postId;
    }

    public Long getMemberId() {
        return memberId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
