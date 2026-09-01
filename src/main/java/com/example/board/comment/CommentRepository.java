package com.example.board.comment;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    @Query("select c from Comment c where c.postId = :postId order by c.createdAt asc, c.id asc")
    List<Comment> findByPostId(@Param("postId") Long postId);

}
