package com.example.board.post;

import java.time.LocalDateTime;

public class PostListItem {

    Long id;
    String title;
    String writerNickname;
    Integer commentCount;
    LocalDateTime createdAt;

    public PostListItem(Long id, String title, String writerNickname, Integer commentCount, LocalDateTime createdAt) {
        this.id = id;
        this.title = title;
        this.writerNickname = writerNickname;
        this.commentCount = commentCount;
        this.createdAt = createdAt;
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

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
