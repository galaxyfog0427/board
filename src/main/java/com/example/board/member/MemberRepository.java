package com.example.board.member;

public interface MemberRepository {

    Long save(Member member);

    Member findById(Long memberId);

    boolean existsByLoginId(String loginId);

    Member findByLoginId(String loginId);

}
