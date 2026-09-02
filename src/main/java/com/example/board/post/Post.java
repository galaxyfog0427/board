package com.example.board.post;

import com.example.board.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "post")
public class Post extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long id;

    private Long memberId;
    private String title;
    private String content;
    private Integer commentCount;

    protected Post() {
    }

    public Post(Long id, Long memberId, String title, String content, Integer commentCount) {
        this.id = id;
        this.memberId = memberId;
        this.title = title;
        this.content = content;
        this.commentCount = commentCount;
    }

    @PrePersist
    void prePersist() {
        if (this.commentCount == null) {
            this.commentCount = 0;
        }
    }

    public void changeTitleAndContent(String title, String content) {
        this.title = title;
        this.content = content;
    }

    public Long getId() {
        return id;
    }

    public Long getMemberId() {
        return memberId;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public Integer getCommentCount() {
        return commentCount;
    }

    @Override
    public String toString() {
        return "Post{" +
                "id=" + id +
                ", memberId=" + memberId +
                ", title='" + title + '\'' +
                ", commentCount=" + commentCount +
                '}';
    }
}
