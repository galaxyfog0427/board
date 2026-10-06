package com.example.board.post.repository;

import com.example.board.post.dto.PostListItem;
import com.example.board.post.dto.PostSearchCondition;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface PostRepositoryCustom {

    Page<PostListItem> search(PostSearchCondition condition, Pageable pageable);

}
