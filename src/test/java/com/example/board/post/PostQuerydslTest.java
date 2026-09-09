package com.example.board.post;


import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.example.board.member.QMember.member;
import static com.example.board.post.QPost.post;
import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
public class PostQuerydslTest {

    @Autowired
    JPAQueryFactory queryFactory;

    @Autowired
    PostRepository postRepository;

    @Autowired
    MemberRepository memberRepository;

    @Test
    void searchByTitle() {
        Member savedMember = memberRepository.save(
                new Member(null, "querydslTest", "test1234!", "querydsl테스터", null, null)
        );

        postRepository.save(new Post(null, savedMember, "querydsl 연습 글", "content", null));
        postRepository.save(new Post(null, savedMember, "other title", "content", null));

        List<Post> result = queryFactory
                .selectFrom(post)
                .join(post.member, member).fetchJoin()
                .where(post.title.contains("querydsl"))
                .orderBy(post.createdAt.desc())
                .fetch();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("querydsl 연습 글");
        assertThat(result.get(0).getMember().getNickname()).isEqualTo("querydsl테스터");
    }

    @Test
    void dynamicSearch() {
        Member savedMember = memberRepository.save(
                new Member(null, "querydslTester2", "test1234!", "동적검색테스터", null, null)
        );

        postRepository.save(new Post(null, savedMember, "querydsl 동적쿼리 연습", "content1", null));
        postRepository.save(new Post(null, savedMember, "other title", "content2", null));

        List<Post> result = searchPost("동적쿼리 연습", "동적검색테스터");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getTitle()).isEqualTo("querydsl 동적쿼리 연습");
    }

    private List<Post> searchPost(String titleKeyword, String writerNickname) {
        return queryFactory
                .selectFrom(post)
                .join(post.member, member).fetchJoin()
                .where(
                        titleContains(titleKeyword),
                        writerNicknameEq(writerNickname)
                )
                .orderBy(post.createdAt.desc())
                .fetch();
    }

    private BooleanExpression titleContains(String titleKeyword) {
        return StringUtils.hasText(titleKeyword) ? post.title.contains(titleKeyword) : null;
    }

    private BooleanExpression writerNicknameEq(String writerNickname) {
        return StringUtils.hasText(writerNickname) ? member.nickname.eq(writerNickname) : null;
    }

}
