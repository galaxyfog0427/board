package com.example.board.post;

import java.time.LocalDateTime;

public class PostListItem {

    Long id;
    String title;
    String writerNickname;
    Integer commentCount;
    Long viewCount;
    LocalDateTime createdAt;

    public PostListItem(Long id, String title, String writerNickname, Integer commentCount, Long viewCount,
                        LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.writerNickname = writerNickname;
        this.commentCount = commentCount;
        this.viewCount = viewCount;
        this.createdAt = createdAt;
    }

    public void addPendingViews(long pendingViews) {
        this.viewCount += pendingViews;
    }

    public Long getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public String getWriterNickname() {
        return writerNickname;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    public Long getViewCount() {
        return viewCount;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
