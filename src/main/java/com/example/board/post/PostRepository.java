package com.example.board.post;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long> {


    default List<Post> findAll() {
        return findAll(Sort.by(Sort.Direction.DESC, "createdAt", "id"));
    };

    @Modifying(clearAutomatically = true)
    @Query("update Post p set p.commentCount = p.commentCount + 1 where p.id = :postId")
    void incrementCommentCount(@Param("postId") Long postId);

    @Query(value = """
                select new com.example.board.post.PostListItem(p.id, p.title, p.member.nickname, p.commentCount, p.createdAt)
                from Post p
            """,
            countQuery = "select count(p) from Post p")
    Page<PostListItem> findAllWithWriter(Pageable pageable);

}
