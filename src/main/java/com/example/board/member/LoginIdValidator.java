package com.example.board.member;

import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
public class LoginIdValidator implements Validator {

    private final MemberRepository memberRepository;

    public LoginIdValidator(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    @Override
    public boolean supports(Class<?> clazz) {
        return MemberJoinForm.class.isAssignableFrom(clazz);
    }

    @Override
    public void validate(Object target, Errors errors) {
        MemberJoinForm form = (MemberJoinForm) target;

        if (form.getLoginId() != null && memberRepository.existsByLoginId(form.getLoginId())) {
            errors.rejectValue("loginId", "duplicate", "이미 사용 중인 로그인 ID입니다.");
        }
    }
}
