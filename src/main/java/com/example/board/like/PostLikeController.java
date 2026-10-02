package com.example.board.like;

import com.example.board.login.MemberDetails;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PostLikeController {

    private final PostLikeService postLikeService;

    public PostLikeController(PostLikeService postLikeService) {
        this.postLikeService = postLikeService;
    }

    @PostMapping("/posts/{postId}/like")
    public String like(@PathVariable("postId") Long postId,
                       @AuthenticationPrincipal MemberDetails memberDetails,
                       RedirectAttributes redirectAttributes) {
        postLikeService.like(postId, memberDetails.getMember().getId());
        redirectAttributes.addAttribute("postId", postId);
        return "redirect:/posts/{postId}";
    }

    @PostMapping("/posts/{postId}/unlike")
    public String unlike(@PathVariable("postId") Long postId,
                         @AuthenticationPrincipal MemberDetails memberDetails,
                         RedirectAttributes redirectAttributes) {
        postLikeService.unlike(postId, memberDetails.getMember().getId());
        redirectAttributes.addAttribute("postId", postId);
        return "redirect:/posts/{postId}";
    }
}
