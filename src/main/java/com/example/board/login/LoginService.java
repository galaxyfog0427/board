package com.example.board.login;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import org.springframework.stereotype.Service;

@Service
public class LoginService {

    private final MemberRepository memberRepository;

    public LoginService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member login(String loginId, String password) {
        Member member = memberRepository.findByLoginId(loginId);
        if (member == null) {
            return null;
        }

        if (!member.getPassword().equals(password)) {
            return null;
        }

        return member;
    }
}
