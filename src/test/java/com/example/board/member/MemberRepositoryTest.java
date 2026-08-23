package com.example.board.member;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class MemberRepositoryTest {

    @Autowired
    MemberRepository repository;

    @Test
    void crud() {
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