package com.example.board.file;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.domain.Post;
import com.example.board.post.repository.PostRepository;
import com.example.board.support.IntegrationTestSupport;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class PostFileControllerTest extends IntegrationTestSupport {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    PostFileRepository postFileRepository;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    FileStore fileStore;

    private final List<String> storedFileNames = new ArrayList<>();

    @AfterEach
    void deleteStoredFiles() throws Exception {
        for (String name : storedFileNames) {
            Files.deleteIfExists(fileStore.load(name).getFile().toPath());
        }
    }

    @Test
    @DisplayName("응답 헤더의 content-type은 Accept 헤더가 아닌 저장된 파일의 확장자를 보고 결정한다")
    void responseContentTypeIsStoreFileExt() throws Exception {
        Long fileId = saveFileOf("cat.png");

        mockMvc.perform(get("/posts/files/" +  fileId + "/view")
                .accept("image/avif"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.IMAGE_PNG));
    }

    @Test
    @DisplayName("MediaTypeFactory가 인식하지 못하는 확장자 요청 시, application/octet-stream을 반환한다")
    void unknownExtensionThenReturnOctetStream() throws Exception {
        Long fileId = saveFileOf("cat.zzz");

        mockMvc.perform(get("/posts/files/" +  fileId + "/view"))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaType.APPLICATION_OCTET_STREAM));
    }

    private Long saveFileOf(String originFilename) throws IOException {
        Member member = memberRepository.save(new Member(
                null,
                "test",
                "test1234!",
                "tester",
                null, null
        ));

        Long postId = postRepository.save(new Post(null, member, "title", "content", null)).getId();

        MockMultipartFile multipartFile = new MockMultipartFile(
                "files",
                originFilename,
                "image/png",
                new byte[]{1, 2, 3}
        );
        UploadFile uploadFile = fileStore.storeFile(multipartFile);
        storedFileNames.add(uploadFile.getStoreFileName());

        PostFile postFile = new PostFile(
                null,
                postId,
                uploadFile.getUploadFileName(),
                uploadFile.getStoreFileName(),
                multipartFile.getSize(),
                null
        );

        return postFileRepository.save(postFile);
    }

}