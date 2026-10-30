package com.example.board.file;

import java.util.List;
import java.util.Optional;

public interface PostFileRepository {

    Long save(PostFile postFile);

    List<PostFile> findByPostId(Long postId);

    Optional<PostFile> findById(Long fileId);
}
