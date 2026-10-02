package com.example.board.like;

import com.example.board.login.MemberDetails;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.Post;
import com.example.board.post.PostRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostLikeControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    PostLikeService postLikeService;

    @Test
    @DisplayName("로그인 회원이 좋아요를 누르면 저장되고 상세 페이지로 돌아간다")
    void like() throws Exception {
        Member member = memberRepository.save(new Member(null, "likeCtrl1", "test1234!", "좋아요1", null, null));
        Long postId = postRepository.save(new Post(null, member, "title", "content", null)).getId();

        mockMvc.perform(post("/posts/" + postId + "/like")
                        .with(user(new MemberDetails(member)))
                        .with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        assertThat(postLikeService.countLike(postId)).isOne();
    }

    @Test
    @DisplayName("비로그인 사용자는 로그인 페이지로 이동한다")
    void likeWithoutLogin() throws Exception {
        mockMvc.perform(post("/posts/1/like").with(csrf()))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("CSRF 토큰이 없으면 403")
    void likeWithoutCsrf() throws Exception {
        Member member = memberRepository.save(new Member(null, "likeCtrl2", "test1234!", "좋아요2", null, null));

        mockMvc.perform(post("/posts/1/like").with(user(new MemberDetails(member))))
                .andExpect(status().isForbidden());
    }
}