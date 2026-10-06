package com.example.board.post;

import java.util.List;

public interface PostFileRepository {

    Long save(PostFile postFile);

    List<PostFile> findByPostId(Long postId);

    PostFile findById(Long fileId);
}
