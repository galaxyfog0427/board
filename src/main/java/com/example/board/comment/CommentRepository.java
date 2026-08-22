package com.example.board.comment;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

@Repository
public class CommentRepository {

    private final DataSource dataSource;

    public CommentRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long save(Comment comment) throws SQLException {
        String sql = "INSERT INTO comment(post_id, member_id, content) " +
                "VALUES (?, ?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setLong(1, comment.getPostId());
            pstmt.setLong(2, comment.getMemberId());
            pstmt.setString(3, comment.getContent());
            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                return rs.getLong(1);
            }

            throw new SQLException("id generated failed");
        } finally {
            close(con, pstmt, rs);
        }
    }

    public Comment findById(Long commentId) throws SQLException {
        String sql = "SELECT comment_id, post_id, member_id, content, created_at, updated_at " +
                "FROM comment WHERE comment_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, commentId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                Comment comment = new Comment(
                        rs.getLong("comment_id"),
                        rs.getLong("post_id"),
                        rs.getLong("member_id"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );

                return comment;
            } else {
                throw new NoSuchElementException("comment not found commentId = " + commentId);
            }
        } catch (SQLException e) {
            System.out.println("db error");
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    private void close(Connection con, PreparedStatement pstmt, ResultSet rs) {
        if (rs != null) {
            try {
                rs.close();
            } catch (SQLException e) {
                System.out.println("ResultSet close error: " + e.getMessage());
            }
        }

        if (pstmt != null) {
            try {
                pstmt.close();
            } catch (SQLException e) {
                System.out.println("PreparedStatement close error: " + e.getMessage());
            }
        }

        if (con != null) {
            try {
                con.close();
            } catch (SQLException e) {
                System.out.println("Connection close error: " + e.getMessage());
            }
        }
    }

}
