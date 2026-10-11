package com.example.board.member;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;
    private final PasswordEncoder passwordEncoder;
    private final ApplicationEventPublisher eventPublisher;

    public MemberService(MemberRepository memberRepository,
                         PasswordEncoder passwordEncoder,
                         ApplicationEventPublisher eventPublisher) {
        this.memberRepository = memberRepository;
        this.passwordEncoder = passwordEncoder;
        this.eventPublisher = eventPublisher;
    }

    @Transactional
    public Long join(String loginId, String rawPassword, String nickname) {
        Member member = new Member(
                null,
                loginId,
                passwordEncoder.encode(rawPassword),
                nickname,
                null,
                null
        );
        Member savedMember = memberRepository.save(member);
        eventPublisher.publishEvent(new MemberJoinedEvent(savedMember.getId(), nickname));
        return savedMember.getId();
    }

}
