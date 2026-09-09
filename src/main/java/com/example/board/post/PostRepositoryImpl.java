package com.example.board.post;

import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;
import org.springframework.util.StringUtils;

import java.util.List;

import static com.example.board.member.QMember.member;
import static com.example.board.post.QPost.post;

@Repository
public class PostRepositoryImpl implements PostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public PostRepositoryImpl(JPAQueryFactory queryFactory) {
        this.queryFactory = queryFactory;
    }

    @Override
    public Page<PostListItem> search(PostSearchCondition condition, Pageable pageable) {
        List<PostListItem> content = queryFactory
                .select(Projections.constructor(PostListItem.class,
                        post.id, post.title, member.nickname, post.commentCount, post.createdAt))
                .from(post)
                .join(post.member, member)
                .where(
                        titleContains(condition.title()),
                        writerNicknameEq(condition.writerNickname())
                )
                .orderBy(post.createdAt.desc(), post.id.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> countQuery = queryFactory
                .select(post.count())
                .from(post)
                .where(
                        titleContains(condition.title()),
                        writerNicknameEq(condition.writerNickname())
                );

        if (StringUtils.hasText(condition.writerNickname())) {
            countQuery.join(post.member, member);
        }

        return new PageImpl<>(content, pageable, countQuery.fetchOne());
    }

    private BooleanExpression titleContains(String title) {
        return StringUtils.hasText(title) ? post.title.contains(title) : null;
    }

    private BooleanExpression writerNicknameEq(String writerNickname) {
        return StringUtils.hasText(writerNickname) ? member.nickname.eq(writerNickname) : null;
    }

}
