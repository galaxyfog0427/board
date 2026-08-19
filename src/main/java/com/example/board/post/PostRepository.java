package com.example.board.post;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

public class PostRepository {

    private final DataSource dataSource;

    public PostRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long save(Post post) throws SQLException {
        String sql = "INSERT INTO post(title, content, created_at) VALUES (?, ?, NOW())";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, post.getTitle());
            pstmt.setString(2, post.getContent());
            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                return rs.getLong(1);
            }

            throw new SQLException("id generation failed");
        } finally {
            close(con, pstmt, null);
        }
    }

    public Post findById(Long id) throws SQLException {

        String sql = """
                SELECT id, title, content, created_at, updated_at
                FROM post
                WHERE id = ?
                """;

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, id);
            rs = pstmt.executeQuery();

            if (rs.next()) {
                Post post = new Post(
                        rs.getLong("id"),
                        rs.getString("title"),
                        rs.getString("content"),
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at") != null ?
                                rs.getTimestamp("updated_at").toLocalDateTime() : null);

                return post;
            } else {
                throw new NoSuchElementException("post not found postId = " + id);
            }
        } catch (SQLException e) {
            System.out.println("db error");
            throw e;
        } finally {
            close(con, pstmt, rs);
        }
    }

    public void update(Long id, String title, String content) throws SQLException {

        String sql = """
                UPDATE post
                SET title = ?, content = ?, updated_at = NOW()
                WHERE id = ?;
                """;

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setString(1, title);
            pstmt.setString(2, content);
            pstmt.setLong(3, id);
            pstmt.executeUpdate();
        } finally {
            close(con, pstmt, null);
        }

    }

    public void delete(Long id) throws SQLException {

        String sql = "DELETE FROM post WHERE id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, id);
            pstmt.executeUpdate();
        } finally {
            close(con, pstmt, null);
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

