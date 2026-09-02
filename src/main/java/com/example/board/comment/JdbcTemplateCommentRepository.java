package com.example.board.comment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.List;

//@Repository
public class JdbcTemplateCommentRepository /*implements CommentRepository*/ {

    private final JdbcTemplate template;

    public JdbcTemplateCommentRepository(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    public Long save(Comment comment) {
        String sql = "INSERT INTO comment(post_id, member_id, content) " +
                "VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, comment.getPostId());
            pstmt.setLong(2, comment.getMemberId());
            pstmt.setString(3, comment.getContent());
            return pstmt;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public Comment findById(Long commentId) {
        String sql = "SELECT comment_id, post_id, member_id, content, created_at, updated_at " +
                "FROM comment WHERE comment_id = ?";

        return template.queryForObject(sql, commentRowMapper(), commentId);
    }

    public List<Comment> findByPostId(Long postId) {
        String sql = """
                SELECT comment_id, post_id, member_id, content, created_at, updated_at
                FROM comment
                WHERE post_id = ?
                ORDER BY created_at, comment_id
                """;

        return template.query(sql, commentRowMapper(), postId);
    }

    private RowMapper<Comment> commentRowMapper() {
        return (rs, rowNum) -> new Comment(
                rs.getLong("comment_id"),
                rs.getLong("post_id"),
                rs.getLong("member_id"),
                rs.getString("content")
        );
    }
}
