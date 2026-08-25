package com.example.board.post;

import java.util.List;

public interface PostRepository {

    Long save(Post post);

    Post findById(Long postId);

    List<Post> findAll();

    void update(Long postId, String title, String content);

    void delete(Long postId);

    void incrementCommentCount(Long postId);

}
