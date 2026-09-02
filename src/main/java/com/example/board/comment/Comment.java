package com.example.board.comment;

import com.example.board.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "comment")
public class Comment extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long id;

    private Long postId;
    private Long memberId;
    private String content;

    protected Comment() {
    }

    public Comment(Long id, Long postId, Long memberId, String content) {
        this.id = id;
        this.postId = postId;
        this.memberId = memberId;
        this.content = content;
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

    public String getContent() {
        return content;
    }


    @Override
    public String toString() {
        return "Comment{" +
                "id=" + id +
                ", postId=" + postId +
                ", memberId=" + memberId +
                ", content='" + content +
                '}';
    }
}
