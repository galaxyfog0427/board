package com.example.board.comment;

import com.example.board.login.MemberDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class CommentController {

    private final CommentService commentService;

    public CommentController(CommentService commentService) {
        this.commentService = commentService;
    }

    @PostMapping("/posts/{postId}/comments")
    public String write(@PathVariable("postId") Long postId,
                        @Validated @ModelAttribute CommentSaveForm commentSaveForm,
                        BindingResult bindingResult,
                        @AuthenticationPrincipal MemberDetails memberDetails,
                        RedirectAttributes redirectAttributes) {
        redirectAttributes.addAttribute("postId", postId);

        if (bindingResult.hasErrors()) {
            redirectAttributes.addFlashAttribute("commentError", "올바르지 않은 댓글 작성입니다.");
            return "redirect:/posts/{postId}";
        }

        commentService.write(postId, memberDetails.getMemberId(), commentSaveForm.getContent());

        return "redirect:/posts/{postId}";
    }
}
