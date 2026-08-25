package com.example.board.member;

import org.junit.jupiter.api.DisplayName;
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

    @Test
    @DisplayName("existsByLoginId()는 존재 여부를 정확히 반환한다.")
    void existsByLoginId() {
        Member member = new Member(null, "dupCheckId", "test1234!", "중복체크", null, null, null, null);
        repository.save(member);

        assertThat(repository.existsByLoginId("dupCheckId")).isTrue();
        assertThat(repository.existsByLoginId("neverUsedId")).isFalse();
    }

    @Test
    @DisplayName("findByLoginId()는 없는 아이디면 null을 반환한다.")
    void findByLoginId() {
        Member member = new Member(null, "loginFindId", "test1234!", "로그인테스터", null, null, null, null);
        repository.save(member);

        Member foundMember = repository.findByLoginId("loginFindId");
        assertThat(foundMember.getNickname()).isEqualTo("로그인테스터");

        assertThat(repository.findByLoginId("neverUsedId")).isNull();
    }
}