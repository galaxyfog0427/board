package com.example.board.member;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.WebDataBinder;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberRepository memberRepository;
    private final LoginIdValidator loginIdValidator;

    public MemberController(MemberRepository memberRepository, LoginIdValidator loginIdValidator) {
        this.memberRepository = memberRepository;
        this.loginIdValidator = loginIdValidator;
    }

    @InitBinder
    public void init(WebDataBinder dataBinder) {
        dataBinder.addValidators(loginIdValidator);
    }

    @GetMapping("/add")
    public String save(Model model) {
        model.addAttribute("memberJoinForm", new MemberJoinForm());
        return "members/addForm";
    }

    @PostMapping("/add")
    public String save(@Validated @ModelAttribute MemberJoinForm memberJoinForm, BindingResult bindingResult) {
        if (bindingResult.hasErrors()) {
            return "members/addForm";
        }

        Member member = new Member(
                null,
                memberJoinForm.getLoginId(),
                memberJoinForm.getPassword(),
                memberJoinForm.getNickname(),
                null, null, null, null
        );
        memberRepository.save(member);
        return "redirect:/login";
    }
}
