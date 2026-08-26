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
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.*;
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

    @Test
    @DisplayName("로그인 성공 시 세선에 회원 정보가 저장되고 redirectURL로 이동한다")
    void loginSuccess() throws Exception {
        memberRepository.save(
                new Member(null, "loginMockTester", "test1234!", "tester", null, null, null, null)
        );

        MvcResult result = mockMvc.perform(post("/login")
                        .param("loginId", "loginMockTester")
                        .param("password", "test1234!")
                        .param("redirectURL", "/posts/add"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/add"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNotNull();
        assertThat(session.getAttribute(SessionConst.LOGIN_MEMBER)).isNotNull();
    }

    @Test
    @DisplayName("비밀번호가 틀리면 로그인 폼으로 되돌아가고 세션이 생성되지 않는다")
    void loginFail() throws Exception {
        memberRepository.save(
                new Member(null, "loginMockTester", "test1234!", "tester", null, null, null, null)
        );

        MvcResult result = mockMvc.perform(post("/login")
                        .param("loginId", "loginMockTester")
                        .param("password", "test5678!"))
                .andExpect(status().isOk())
                .andExpect(view().name("login/loginForm"))
                .andReturn();

        MockHttpSession session = (MockHttpSession) result.getRequest().getSession(false);
        assertThat(session).isNull();
    }

}