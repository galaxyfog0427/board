package com.example.board.member;

import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.sql.SQLException;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class MemberRepositoryTest {

    @Autowired
    MemberRepository repository;

    @Test
    void crud() throws SQLException {
        //create
        Member member = new Member(
                null,
                "testId",
                "test1234!",
                "tester",
                null,
                null,
                null,
                null);

        Long savedMemberId = repository.save(member);

        //read
        Member foundMember = repository.findById(savedMemberId);
        assertThat(foundMember.getLoginId()).isEqualTo(member.getLoginId());
        assertThat(foundMember.getPassword()).isEqualTo(member.getPassword());
        assertThat(foundMember.getNickname()).isEqualTo(member.getNickname());
        assertThat(foundMember.getStatus()).isEqualTo("ACTIVE");
    }

}