package com.example.board.post.web;

import com.example.board.global.redis.RedisKeys;
import com.example.board.global.web.VisitorCookieManager;
import com.example.board.login.MemberDetails;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.domain.Post;
import com.example.board.post.repository.PostRepository;
import com.example.board.view.ViewCountService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
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

    @Autowired
    StringRedisTemplate redisTemplate;

    List<String> redisKeysToDelete = new ArrayList<>();
    @Autowired
    private ViewCountService viewCountService;

    @AfterEach
    void cleanUpRedis() {
        redisKeysToDelete.add(RedisKeys.pendingViews());
        redisTemplate.delete(redisKeysToDelete);
    }

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
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("로그인한 상태면 게시글 작성 페이지에 정상 접근된다")
    void addFormWithLogin() throws Exception {
        Long memberId = memberRepository.save(
                new Member(null, "mockMvcTester", "test1234!", "목테스터", null, null)
        ).getId();
        Member loginMember = memberRepository.findById(memberId).get();

        mockMvc.perform(get("/posts/add")
                        .with(user(new MemberDetails(loginMember))))
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
                        .with(user(new MemberDetails(loginMember)))
                        .with(csrf())
                        .param("title", "목 테스트 제목")
                        .param("content", "목 테스트 내용"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("/posts/*"));
    }

    @Test
    @DisplayName("다른 회원이 작성한 게시글을 수정하려 하면 403이 발생한다")
    void editFormForbidden() throws Exception {
        Member writer = memberRepository.save(
                new Member(null, "mockMvcWriter", "test1234!", "작성자", null, null));
        Long otherId = memberRepository.save(
                new Member(null, "mockMvcOther", "test1234!", "다른사람", null, null)
        ).getId();
        Member otherMember = memberRepository.findById(otherId).get();

        Post post = new Post(null, writer, "title", "content", null);
        Long postId = postRepository.save(post).getId();

        mockMvc.perform(get("/posts/" + postId + "/edit")
                        .with(user(new MemberDetails(otherMember))))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("비회원이 상세 조회하면 visitor 쿠키가 발급되고, 같은 쿠키로 재조회해도 조회수는 1이다")
    void anonymousViewCountedOnce() throws Exception {
        Member writer = memberRepository.save(
                new Member(null, "viewWriter", "test1234!", "조회작성자", null, null)
        );
        Long postId = postRepository.save(
                new Post(null, writer, "title", "content", null)
        ).getId();

        MvcResult result = mockMvc.perform(get("/posts/" + postId))
                .andExpect(status().isOk())
                .andExpect(cookie().exists(VisitorCookieManager.COOKIE_NAME))
                .andExpect(model().attribute("viewCount", 1L))
                .andReturn();

        Cookie visitorCookie = result.getResponse().getCookie(VisitorCookieManager.COOKIE_NAME);
        redisKeysToDelete.add(RedisKeys.viewDedupForVisitor(postId, visitorCookie.getValue()));

        mockMvc.perform(get("/posts/" + postId).cookie(visitorCookie))
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(VisitorCookieManager.COOKIE_NAME))
                .andExpect(model().attribute("viewCount", 1L));
    }

    @Test
    @DisplayName("로그인 회원은 visitor 쿠키 없이 회원 기준으로 조회수가 증가한다")
    void memberViewCounted() throws Exception {
        Member writer = memberRepository.save(
                new Member(null, "viewWriter2", "test1234!", "조회작성자2", null, null)
        );
        Long postId = postRepository.save(
                new Post(null, writer, "title", "content", null)
        ).getId();
        redisKeysToDelete.add(RedisKeys.viewDedupForMember(postId, writer.getId()));

        mockMvc.perform(get("/posts/" + postId).with(user(new MemberDetails(writer))))
                .andExpect(status().isOk())
                .andExpect(cookie().doesNotExist(VisitorCookieManager.COOKIE_NAME))
                .andExpect(model().attribute("viewCount", 1L));
    }

    @Test
    @DisplayName("존재하지 않는 게시글은 404이고 Redis에 아무것도 남기지 않는다")
    void notFoundPostLeavesNothingInRedis() throws Exception {
        mockMvc.perform(get("/posts/999999999999999"))
                .andExpect(status().isNotFound());

        assertThat(redisTemplate.opsForZSet().score(RedisKeys.pendingViews(), "999999999999999"));
    }

    @Test
    @DisplayName("로그인한 회원이 목록을 열면 닉네임과 함께 정상 표시된다")
    void listWithLogin() throws Exception {
        Member member = memberRepository.save(new Member(
                null, "listLoginTester", "test1234!", "목록테스터", null, null
        ));

        mockMvc.perform(get("/posts").with(user(new MemberDetails(member))))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("목록테스터님 환영합니다")));
    }
}