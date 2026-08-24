package com.example.board.post;

import com.example.board.comment.Comment;
import com.example.board.comment.CommentRepository;
import com.example.board.login.SessionConst;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;

    public PostController(PostRepository postRepository, CommentRepository commentRepository, MemberRepository memberRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.memberRepository = memberRepository;
    }

    @GetMapping
    public String list(HttpServletRequest request, Model model) {
        List<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);

        HttpSession session = request.getSession(false);
        Member loginMember = session != null ? (Member) session.getAttribute(SessionConst.LOGIN_MEMBER) : null;
        model.addAttribute("loginMember", loginMember);

        return "post/list";
    }

    @GetMapping("/{postId}")
    public String detail(@PathVariable("postId") Long postId, Model model) {
        Post post = postRepository.findById(postId);
        Member writer = memberRepository.findById(post.getMemberId());
        List<Comment> comments = commentRepository.findByPostId(postId);
        model.addAttribute("post", post);
        model.addAttribute("writer", writer);
        model.addAttribute("comments", comments);
        return "post/detail";
    }

    @GetMapping("/add")
    public String addForm(HttpServletRequest request, Model model) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConst.LOGIN_MEMBER) == null) {
            return "redirect:/login";
        }

        model.addAttribute("postSaveForm", new PostSaveForm());
        return "post/addForm";
    }

    @PostMapping("/add")
    public String save(@Validated @ModelAttribute PostSaveForm postSaveForm, BindingResult bindingResult,
                       HttpServletRequest request, RedirectAttributes redirectAttributes) {

        HttpSession session = request.getSession(false);
        Member loginMember = session != null ? (Member) session.getAttribute(SessionConst.LOGIN_MEMBER) : null;
        if (loginMember == null) {
            return "redirect:/login";
        }

        if (bindingResult.hasErrors()) {
            return "post/addForm";
        }

        Post post = new Post(
                null,
                loginMember.getId(),
                postSaveForm.getTitle(),
                postSaveForm.getContent(),
                null,
                null,
                null);
        Long savedPostId = postRepository.save(post);

        redirectAttributes.addAttribute("postId", savedPostId);
        return "redirect:/posts/{postId}";
    }

    @GetMapping("/{postId}/edit")
    public String editForm(@PathVariable("postId") Long postId, Model model) {
        Post post = postRepository.findById(postId);

        PostEditForm postEditForm = new PostEditForm();
        postEditForm.setTitle(post.getTitle());
        postEditForm.setContent(post.getContent());

        model.addAttribute("postEditForm", postEditForm);
        model.addAttribute("postId", postId);
        return "post/editForm";
    }

    @PostMapping("/{postId}/edit")
    public String edit(@PathVariable("postId") Long postId, @Validated @ModelAttribute PostEditForm postEditForm, BindingResult bindingResult) {

        if (bindingResult.hasErrors()) {
            return "post/editForm";
        }

        postRepository.update(postId, postEditForm.getTitle(), postEditForm.getContent());
        return "redirect:/posts/{postId}";
    }
}
