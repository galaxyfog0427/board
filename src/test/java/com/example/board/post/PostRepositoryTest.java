package com.example.board.post;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class PostRepositoryTest {

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void crud() {
        Member member = new Member(null, "postTester", "test1234!", "테스터", null, null);
        Member savedMember = memberRepository.save(member);

        //create
        Post post = new Post(
                null,
                savedMember,
                "JDBC 게시글",
                "순수 JDBC로 저장",
                null
        );
        Long savedPostId = postRepository.save(post).getId();

        //read
        Post foundPost = postRepository.findById(savedPostId).get();
        assertThat(foundPost.getTitle()).isEqualTo(post.getTitle());
        assertThat(foundPost.getContent()).isEqualTo(post.getContent());

        //update
        post.changeTitleAndContent("수정된 제목", "수정된 내용");
        Post updatedPost = postRepository.findById(savedPostId).get();
        System.out.println(updatedPost);

        //delete
        postRepository.deleteById(savedPostId);
        postRepository.flush();
        assertThat(postRepository.findById(savedPostId)).isEmpty();
    }

    @Test
    @DisplayName("findAll()은 최신 게시글이 먼저 오도록 정렬한다")
    void findAllOrderedByLatest() {
        Member member = new Member(null, "listTester", "test1234!", "리스트테스트", null, null);
        Member savedMember = memberRepository.save(member);

        Long firstPostId = postRepository.save(
                new Post(null, savedMember, "첫 번째 글", "내용1", null)).getId();
        Long secondPostId = postRepository.save(
                new Post(null, savedMember, "두 번째 글", "내용2", null)).getId();
        Long thirdPostId = postRepository.save(
                new Post(null, savedMember, "세 번째 글", "내용3", null)).getId();

        List<Post> posts = postRepository.findAll();
        assertThat(posts.get(0).getId()).isEqualTo(thirdPostId);
        assertThat(posts.get(1).getId()).isEqualTo(secondPostId);
        assertThat(posts.get(2).getId()).isEqualTo(firstPostId);
    }

    @Test
    @DisplayName("findAll(Pageable)은 요청한 사이즈만큼 잘라서 반환하고 전체 개수를 함께 제공한다")
    void findAllWithPageable() {
        long beforeCount = postRepository.count();

        Member member = new Member(null, "pageTester", "test1234!", "페이지테스터", null, null);
        Member savedMember = memberRepository.save(member);

        for (int i = 1; i <= 15; i++) {
            postRepository.save(new Post(null, savedMember, "제목" + i, "내용" + i, null));
        }

        Page<Post> firstPage = postRepository.findAll(
                PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id"))
        );

        assertThat(firstPage.getContent()).hasSize(10);
        assertThat(firstPage.getTotalElements()).isEqualTo(beforeCount + 15);
        assertThat(firstPage.hasNext()).isTrue();
    }

    @Test
    @DisplayName("findAllWithWriter()는 작성자 닉네임을 함께 조회하고, count 쿼리는 JOIN 없이 동작한다")
    void findAllWithWriter() {
        long beforeCount = postRepository.count();

        Member member = new Member(null, "writerTester", "test1234!", "글쓴이", null, null);
        Member savedMember = memberRepository.save(member);

        Long savedPostId = postRepository.save(new Post(null, savedMember, "title", "content", null)).getId();

        Page<PostListItem> page = postRepository.findAllWithWriter(PageRequest.of(0, 10, Sort.by(Sort.Direction.DESC, "id")));

        assertThat(page.getTotalElements()).isEqualTo(beforeCount + 1);
        assertThat(page.getContent().get(0).getId()).isEqualTo(savedPostId);
        assertThat(page.getContent().get(0).getWriterNickname()).isEqualTo("글쓴이");
    }
}