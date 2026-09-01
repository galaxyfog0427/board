package com.example.board.post;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

//@Repository
public class JdbcTemplatePostRepository /*implements PostRepository*/ {

    private final JdbcTemplate template;

    public JdbcTemplatePostRepository(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }


    public Long save(Post post) {
        String sql = "INSERT INTO post(member_id, title, content) VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, post.getMemberId());
            pstmt.setString(2, post.getTitle());
            pstmt.setString(3, post.getContent());
            return pstmt;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public Post findById(Long postId) {
        String sql = """
                SELECT post_id, member_id, title, content, comment_count, created_at, updated_at
                FROM post
                WHERE post_id = ?
                """;

        try {
            return template.queryForObject(sql, postRowMapper(), postId);
        } catch (EmptyResultDataAccessException e) {
            throw new PostNotFoundException("존재하지 않는 게시글입니다. postId=" + postId);
        }
    }

    public List<Post> findAll() {
        String sql = """
                SELECT post_id, member_id, title, content, comment_count, created_at, updated_at
                FROM post
                ORDER BY created_at DESC, post_id DESC
                """;

        return template.query(sql, postRowMapper());
    }

    public void update(Long postId, String title, String content) {
        String sql = """
                UPDATE post
                SET title = ?, content = ?
                WHERE post_id = ?;
                """;

        template.update(sql, title, content, postId);
    }

    public void delete(Long postId) {
        String sql = "DELETE FROM post WHERE post_id = ?";
        template.update(sql, postId);
    }

    public void incrementCommentCount(Long postId) {
        String sql = "UPDATE post SET comment_count = comment_count + 1 WHERE post_id = ?";
        template.update(sql, postId);
    }

    private RowMapper<Post> postRowMapper() {
        return (rs, rowNum) -> new Post(
                rs.getLong("post_id"),
                rs.getLong("member_id"),
                rs.getString("title"),
                rs.getString("content"),
                rs.getInt("comment_count"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }

}

