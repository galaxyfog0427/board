package com.example.board.post.web;

import com.example.board.file.PostFileRepository;
import com.example.board.login.MemberDetails;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.repository.PostRepository;
import com.example.board.support.IntegrationTestSupport;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;

@SpringBootTest
@AutoConfigureMockMvc
public class PostSaveCompensationTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @MockitoBean
    PostFileRepository postFileRepository;

    @Value("${file.dir}")
    String fileDir;

    Member member;

    @BeforeEach
    void setUp() {
        member = memberRepository.save(
                new Member(
                        null,
                        "test",
                        "test1234!",
                        "tester",
                        null,
                        null));
    }

    @AfterEach
    void cleanUp() {
        memberRepository.deleteById(member.getId());
    }


    @Test
    @DisplayName("이름표 저장이 실패하면 게시글도 남지 않고, 올린 파일도 지워진다")
    void compensatesWhenPostFileSaveFails() throws Exception {
        given(postFileRepository.save(any()))
                .willThrow(new RuntimeException("이름표 저장 실패"));
        long postsBefore = postRepository.count();
        long filesBefore = countFiles();

        assertThatThrownBy(() -> {
            mockMvc.perform(multipart("/posts/add")
                    .file(new MockMultipartFile("files",
                            "originFilename",
                            "image/png",
                            new byte[]{1, 2, 3}))
                    .param("title", "title")
                    .param("content", "content")
                    .with(user(new MemberDetails(member)))
                    .with(csrf()));
        }).isInstanceOf(ServletException.class)
                .hasRootCauseMessage("이름표 저장 실패");

        assertThat(postRepository.count()).isEqualTo(postsBefore);
        assertThat(countFiles()).isEqualTo(filesBefore);
    }

    private long countFiles() throws IOException {
        Path dir = Path.of(fileDir);
        Files.createDirectories(dir);
        try (Stream<Path> files = Files.list(dir)) {
            return files.count();
        }
    }
}
