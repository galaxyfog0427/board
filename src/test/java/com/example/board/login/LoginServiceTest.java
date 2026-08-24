package com.example.board.login;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Transactional
class LoginServiceTest {

    @Autowired
    LoginService loginService;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("아이디, 비밀번호가 일치하면 로그인 성공")
    void loginSuccess() {
        memberRepository.save(new Member(null, "loginTester", "correctPw1!", "로그인테스터", null, null, null, null));

        Member loginMember = loginService.login("loginTester", "correctPw1!");

        assertThat(loginMember).isNotNull();
        assertThat(loginMember.getLoginId()).isEqualTo("loginTester");
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인 실패")
    void loginFailWrongPassword() {
        memberRepository.save(new Member(null, "loginTester2", "correctPw1!", "로그인테스터", null, null, null, null));

        Member loginMember = loginService.login("loginTester2", "wrongPassword");

        assertThat(loginMember).isNull();
    }

    @Test
    @DisplayName("존재하지 않는 아이디면 로그인 실패")
    void loginFailNoSuchId() {
        Member loginMember = loginService.login("neverUsedId", "neverUsedPassword");

        assertThat(loginMember).isNull();
    }
}