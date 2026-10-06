package com.example.board.like;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.domain.Post;
import com.example.board.post.repository.PostRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class PostLikeConcurrencyTest {

    @Autowired
    PostLikeService postLikeService;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

    Long postId;
    Long memberId;

    @AfterEach
    void cleanUp() {
        jdbcTemplate.update("DELETE FROM post_like WHERE post_id = ?", postId);
        postRepository.deleteById(postId);
        memberRepository.deleteById(memberId);
    }

    @Test
    @DisplayName("같은 회원이 동시에 10번 좋아요를 눌러도 정확히 1건만 저장되고 1번만 성공한다")
    void concurrentLikes() throws InterruptedException {
        Member member = memberRepository.save(
                new Member(
                        null,
                        "likeRacer",
                        "test1234!",
                        "동시클릭",
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

        int threadCount = 10;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);
        CountDownLatch ready = new CountDownLatch(threadCount);
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threadCount);
        AtomicInteger successCount = new AtomicInteger();
        List<Throwable> errors = Collections.synchronizedList(new ArrayList<>());

        for (int i = 0; i < threadCount; i++) {
            executor.submit(() -> {
                try {
                    ready.countDown();
                    start.await();
                    if (postLikeService.like(postId, memberId)) {
                        successCount.incrementAndGet();
                    }
                } catch (Throwable e) {
                    errors.add(e);
                } finally {
                    done.countDown();
                }
            });
        }

        ready.await();
        start.countDown();
        done.await();
        executor.shutdown();

        assertThat(errors).isEmpty();
        assertThat(successCount.get()).isOne();
        assertThat(postLikeService.countLike(postId)).isOne();
    }
}
