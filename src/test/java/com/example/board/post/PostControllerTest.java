package com.example.board.post;

import com.example.board.login.SessionConst;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Test
    @DisplayName("게시글 목록 조회는 로그인 없어도 200 응답과 목록 화면을 반환한다")
    void list() throws Exception {
        mockMvc.perform(get("/posts"))
                .andExpect(status().isOk())
                .andExpect(view().name("post/list"));
    }

    @Test
    @DisplayName("로그인 없이 게시글 작성 페이지 접근 시 로그인 페이지로 리다이렉트된다")
    void addFormWithoutLogin() throws Exception {
        mockMvc.perform(get("/posts/add"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login?redirectURL=/posts/add"));
    }

    @Test
    @DisplayName("로그인한 상태면 게시글 작성 페이지에 정상 접근된다")
    void addFormWithLogin() throws Exception {
        Long memberId = memberRepository.save(
                new Member(null, "mockMvcTester", "test1234!", "목테스터", null, null)
        ).getId();
        Member loginMember = memberRepository.findById(memberId).get();

        mockMvc.perform(get("/posts/add")
                        .sessionAttr(SessionConst.LOGIN_MEMBER, loginMember))
                .andExpect(status().isOk())
                .andExpect(view().name("post/addForm"));
    }

    @Test
    @DisplayName("로그인한 회원이 게시글을 등록하면 상세 페이지로 리다이렉트된다")
    void saveSuccess() throws Exception {
        Long memberId = memberRepository.save(
                new Member(null, "mockMvcTester", "test1234!", "목테스터", null, null)
        ).getId();
        Member loginMember = memberRepository.findById(memberId).get();

        mockMvc.perform(post("/posts/add")
                        .sessionAttr(SessionConst.LOGIN_MEMBER, loginMember)
                        .param("title", "목 테스트 제목")
                        .param("content", "목 테스트 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/posts/*"));
    }

    @Test
    @DisplayName("다른 회원이 작성한 게시글을 수정하려 하면 403이 발생한다")
    void editFormForbidden() throws Exception {
        Long writerId = memberRepository.save(
                new Member(null, "mockMvcWriter", "test1234!", "작성자", null, null)
        ).getId();
        Long otherId = memberRepository.save(
                new Member(null, "mockMvcOther", "test1234!", "다른사람", null, null)
        ).getId();
        Member otherMember = memberRepository.findById(otherId).get();

        Post post = new Post(null, writerId, "title", "content", null);
        Long postId = postRepository.save(post).getId();

        mockMvc.perform(get("/posts/" + postId + "/edit")
                        .sessionAttr(SessionConst.LOGIN_MEMBER, otherMember))
                .andExpect(status().isForbidden());
    }
}