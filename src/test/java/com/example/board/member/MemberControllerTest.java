package com.example.board.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("정상적인 회원가입은 로그인 페이지로 리다이렉트된다")
    void joinSuccess() throws Exception {
        mockMvc.perform(post("/members/add")
                        .param("loginId", "newJoiner")
                        .param("password", "test1234!")
                        .param("nickname", "tester"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("이미 존재하는 로그인 ID로 가입 시도하면 폼으로 되돌아간다")
    void joinDuplicateLoginId() throws Exception {
        memberRepository.save(
                new Member(null, "duplicatedId", "test1234!", "먼저가입", null, null, null, null)
        );

        mockMvc.perform(post("/members/add")
                        .param("loginId", "duplicatedId")
                        .param("password", "test5678!")
                        .param("nickname", "나중가입"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/addForm"))
                .andExpect(model().attributeHasFieldErrors("memberJoinForm", "loginId"));
    }

    @Test
    @DisplayName("필수값 누락 시 폼으로 되돌아간다")
    void joinBlankTitle() throws Exception {
        mockMvc.perform(post("/members/add")
                        .param("loginId", "")
                        .param("password", "test1234!")
                        .param("nickname", "tester"))
                .andExpect(status().isOk())
                .andExpect(view().name("members/addForm"))
                .andExpect(model().attributeHasFieldErrors("memberJoinForm", "loginId"));
    }
}