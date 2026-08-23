package com.example.board.member;

import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.support.GeneratedKeyHolder;
import org.springframework.jdbc.support.KeyHolder;
import org.springframework.stereotype.Repository;

import javax.sql.DataSource;
import java.sql.*;

@Repository
public class MemberRepository {

    private final JdbcTemplate template;

    public MemberRepository(DataSource dataSource) {
        this.template = new JdbcTemplate(dataSource);
    }

    public Long save(Member member) {
        String sql = "INSERT INTO member(login_id, password, nickname) " +
                "VALUES (?, ?, ?)";
        KeyHolder keyHolder = new GeneratedKeyHolder();

        template.update(connection -> {
            PreparedStatement pstmt = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, member.getLoginId());
            pstmt.setString(2, member.getPassword());
            pstmt.setString(3, member.getNickname());
            return pstmt;
        }, keyHolder);

        return keyHolder.getKey().longValue();
    }

    public Member findById(Long memberId) {
        String sql = "SELECT member_id, login_id, password, nickname, status, withdrawn_at, created_at, updated_at " +
                "FROM member WHERE member_id = ?";

        return template.queryForObject(sql, memberRowMapper(), memberId);
    }



    private RowMapper<Member> memberRowMapper() {
        return (rs, rowNum) -> new Member(
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
    }

}
