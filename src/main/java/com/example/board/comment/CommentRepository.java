package com.example.board.comment;

import java.util.List;

public interface CommentRepository {

    Long save(Comment comment);

    Comment findById(Long commentId);

    List<Comment> findByPostId(Long postId);

}
