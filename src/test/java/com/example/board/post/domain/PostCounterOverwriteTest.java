package com.example.board.post;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.domain.Post;
import com.example.board.post.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class PostCounterOverwriteTest {

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    TransactionTemplate transactionTemplate;

    Long postId;
    Long memberId;

    @AfterEach
    void cleanUp() {
        postRepository.deleteById(postId);
        memberRepository.deleteById(memberId);
    }

    @Test
    @DisplayName("게시글 수정이 그 사이 증가한 댓글 수를 과거 값으로 덮어쓰지 않는다")
    void editDoesNotOverwriteCommentCount() {
        Member member = memberRepository.save(
                new Member(
                        null,
                        "counterTester",
                        "test1234!",
                        "카운터테스터",
                        null,
                        null
                )
        );
        memberId = member.getId();
        postId = postRepository.save(
                new Post(
                        null,
                        member,
                        "title",
                        "content",
                        null
                )
        ).getId();

        // t1: 수정하려는 쪽이 먼저 읽는다 (commentCount = 0)
        Post loaded = postRepository.findById(postId).get();

        // t2: 그 사이 새 댓글이 달려 DB는 1이 된다
        transactionTemplate.executeWithoutResult(status ->
                postRepository.incrementCommentCount(postId));

        // t3: 수정 내용을 저장한다
        loaded.changeTitleAndContent("new title", "new content");
        postRepository.save(loaded);

        Post result = postRepository.findById(postId).get();
        assertThat(result.getTitle()).isEqualTo("new title");
        assertThat(result.getCommentCount()).isOne();
    }
}
