package com.example.board.comment;

import com.example.board.global.redis.RedisKeys;
import com.example.board.login.MemberDetails;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.domain.Post;
import com.example.board.post.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

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
class CommentControllerTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    CommentService commentService;

    @Autowired
    StringRedisTemplate redisTemplate;

    Member member;
    Long postId;
    @Autowired
    private CommentRepository commentRepository;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(new Member(
                null, "commentTester", "test1234!", "댓글러", null, null
        ));
        postId = postRepository.save(new Post(null, member, "title", "content", null)).getId();
    }

    @AfterEach
    void cleanUpRedis() {
        redisTemplate.delete(List.of(
                RedisKeys.pendingViews(),
                RedisKeys.viewDedupForMember(postId, member.getId()),
                RedisKeys.postLikes(postId)
        ));
    }

    @Test
    @DisplayName("로그인 회원이 댓글을 쓰면 저장되고, 댓글 수가 1이 되며 상세로 돌아간다")
    void write() throws Exception {
        mockMvc.perform(post("/posts/" + postId + "/comments")
                        .with(user(new MemberDetails(member)))
                        .with(csrf())
                        .param("content", "첫 댓글"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/posts/" + postId));

        assertThat(commentRepository.findByPostId(postId)).hasSize(1);
        assertThat(postRepository.findById(postId).get().getCommentCount()).isOne();
    }

    @Test
    @DisplayName("비로그인이면 로그인 페이지로 이동한다")
    void writeWithoutLogin() throws Exception {
        mockMvc.perform(post("/posts/" + postId + "/comments")
                        .with(csrf())
                        .param("content", "첫 댓글"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));
    }

    @Test
    @DisplayName("내용이 비어 있으면 저장하지 않고 에러 메시지와 함께 상세로 돌아간다")
    void writeBlank() throws Exception {
        mockMvc.perform(post("/posts/" + postId + "/comments")
                        .with(user(new MemberDetails(member)))
                        .with(csrf())
                        .param("content", " "))
                .andExpect(status().is3xxRedirection())
                .andExpect(flash().attributeExists("commentError"));

        assertThat(commentRepository.findByPostId(postId)).isEmpty();
    }

    @Test
    @DisplayName("없는 게시글이면 404")
    void writeToNotFoundPost() throws Exception {
        mockMvc.perform(post("/posts/999999999/comments")
                        .with(user(new MemberDetails(member)))
                        .with(csrf())
                        .param("content", "댓글"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("CSRF 토큰이 없으면 403")
    void writeWithoutCsrf() throws Exception {
        mockMvc.perform(post("/posts/" + postId + "/comments")
                        .with(user(new MemberDetails(member)))
                        .param("content", "댓글"))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("상세 화면에 댓글 작성자의 닉네임이 보인다")
    void detailShowsWriterNickname() throws Exception {
        mockMvc.perform(post("/posts/" + postId + "/comments")
                .with(user(new MemberDetails(member)))
                .with(csrf())
                .param("content", "닉네임 확인용"));

        mockMvc.perform(get("/posts/" + postId)
                        .with(user(new MemberDetails(member))))
                .andExpect(status().isOk())
                .andExpect(content().string(org.hamcrest.Matchers.containsString("댓글러")));
    }
}