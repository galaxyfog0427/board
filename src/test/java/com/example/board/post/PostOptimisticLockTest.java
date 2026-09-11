package com.example.board.post;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.orm.ObjectOptimisticLockingFailureException;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
public class PostOptimisticLockTest {

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    @DisplayName("동시 수정 시 나중에 저장하는 쪽이 충돌 예외를 받는다")
    void postEditConflictException() {
        Member savedMember = memberRepository.save(new Member(null, "lockTester", "test1234!", "락테스터", null, null));
        Post savedPost = postRepository.save(new Post(null, savedMember, "original title", "original content", null));
        Long postId = savedPost.getId();

        Post postA = postRepository.findById(postId).get();
        Post postB = postRepository.findById(postId).get();

        postA.changeTitleAndContent("A title", "A content");
        postRepository.save(postA);

        postB.changeTitleAndContent("B title", "B content");
        assertThatThrownBy(() -> postRepository.save(postB))
                .isInstanceOf(ObjectOptimisticLockingFailureException.class);

        postRepository.deleteById(postId);
        memberRepository.deleteById(savedMember.getId());

    }
}
