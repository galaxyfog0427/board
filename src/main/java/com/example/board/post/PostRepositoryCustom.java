package com.example.board.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

    Page<PostListItem> search(PostSearchCondition condition, Pageable pageable);

}
