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
    public String list(@SessionAttribute(name = SessionConst.LOGIN_MEMBER, required = false) Member loginMember, Model model) {
        List<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
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
    public String addForm(Model model) {
        model.addAttribute("postSaveForm", new PostSaveForm());
        return "post/addForm";
    }

    @PostMapping("/add")
    public String save(@Validated @ModelAttribute PostSaveForm postSaveForm, BindingResult bindingResult,
                       @SessionAttribute(SessionConst.LOGIN_MEMBER) Member loginMember, RedirectAttributes redirectAttributes) {

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
    public String editForm(@PathVariable("postId") Long postId,
                           @SessionAttribute(SessionConst.LOGIN_MEMBER) Member loginMember,
                           Model model) {
        Post post = postRepository.findById(postId);

        if (!loginMember.getId().equals(post.getMemberId())) {
            throw new IllegalStateException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        PostEditForm postEditForm = new PostEditForm();
        postEditForm.setTitle(post.getTitle());
        postEditForm.setContent(post.getContent());

        model.addAttribute("postEditForm", postEditForm);
        model.addAttribute("postId", postId);
        return "post/editForm";
    }

    @PostMapping("/{postId}/edit")
    public String edit(@PathVariable("postId") Long postId,
                       @Validated @ModelAttribute PostEditForm postEditForm,
                       BindingResult bindingResult,
                       @SessionAttribute(SessionConst.LOGIN_MEMBER) Member loginMember,
                       Model model) {

        Post post = postRepository.findById(postId);

        if (!loginMember.getId().equals(post.getMemberId())) {
            throw new IllegalStateException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", postId);
            return "post/editForm";
        }

        postRepository.update(postId, postEditForm.getTitle(), postEditForm.getContent());
        return "redirect:/posts/{postId}";
    }
}
