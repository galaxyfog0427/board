package com.example.board.member;

import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;
import java.util.NoSuchElementException;

@Repository
public class MemberRepository {

    private final DataSource dataSource;

    public MemberRepository(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Long save(Member member) throws SQLException {
        String sql = "INSERT INTO member(login_id, password, nickname) " +
                "VALUES (?, ?, ?)";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, member.getLoginId());
            pstmt.setString(2, member.getPassword());
            pstmt.setString(3, member.getNickname());
            pstmt.executeUpdate();

            rs = pstmt.getGeneratedKeys();

            if (rs.next()) {
                return rs.getLong(1);
            }

            throw new SQLException("id generate failed");
        } finally {
            close(con, pstmt, rs);
        }
    }

    public Member findById(Long memberId) throws SQLException {
        String sql = "SELECT member_id, login_id, password, nickname, status, withdrawn_at, created_at, updated_at " +
                "FROM member WHERE member_id = ?";

        Connection con = null;
        PreparedStatement pstmt = null;
        ResultSet rs = null;

        try {
            con = dataSource.getConnection();
            pstmt = con.prepareStatement(sql);
            pstmt.setLong(1, memberId);

            rs = pstmt.executeQuery();

            if (rs.next()) {
                Member member = new Member(
                        rs.getLong("member_id"),
                        rs.getString("login_id"),
                        rs.getString("password"),
                        rs.getString("nickname"),
                        rs.getString("status"),
                        rs.getTimestamp("withdrawn_at") != null ?
                                rs.getTimestamp("withdrawn_at").toLocalDateTime() : null,
                        rs.getTimestamp("created_at").toLocalDateTime(),
                        rs.getTimestamp("updated_at").toLocalDateTime()
                );

                return member;
            } else {
                throw new NoSuchElementException("member not found memberId = " + memberId);
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
