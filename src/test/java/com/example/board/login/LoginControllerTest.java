package com.example.board.login;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class LoginControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PasswordEncoder passwordEncoder;

    @Test
    @DisplayName("로그인 성공 시 인증되고 게시글 목록으로 이동한다")
    void loginSuccess() throws Exception {
        memberRepository.save(new Member(
                        null,
                        "loginTester",
                        passwordEncoder.encode("test1234!"),
                        "로그인테스터",
                        null, null));

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("loginId", "loginTester")
                        .param("password", "test1234!"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts"))
                .andExpect(authenticated().withUsername("loginTester"));
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인 폼으로 되돌아가고 인증되지 않는다")
    void loginFail() throws Exception {
        memberRepository.save(new Member(
                null,
                "loginTester2",
                passwordEncoder.encode("test1234!"),
                "로그인테스터",
                null, null));

        mockMvc.perform(post("/login")
                        .with(csrf())
                        .param("loginId", "loginTester2")
                        .param("password", "wrongPassword"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?error"))
                .andExpect(unauthenticated());
    }

}