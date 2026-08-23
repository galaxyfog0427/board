package com.example.board.comment;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class CommentRepository {

    private final JdbcTemplate template;

    public CommentRepository(DataSource dataSource) {
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

    private RowMapper<Comment> commentRowMapper() {
        return (rs, rowNum) -> new Comment(
                rs.getLong("comment_id"),
                rs.getLong("post_id"),
                rs.getLong("member_id"),
                rs.getString("content"),
                rs.getTimestamp("created_at").toLocalDateTime(),
                rs.getTimestamp("updated_at").toLocalDateTime()
        );
    }
}
