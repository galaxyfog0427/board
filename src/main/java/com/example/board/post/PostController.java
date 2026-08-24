package com.example.board.post;

import com.example.board.comment.Comment;
import com.example.board.comment.CommentRepository;
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

    public PostController(PostRepository postRepository, CommentRepository commentRepository) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping
    public String list(Model model) {
        List<Post> posts = postRepository.findAll();
        model.addAttribute("posts", posts);
        return "post/list";
    }

    @GetMapping("/{postId}")
    public String detail(@PathVariable("postId") Long postId, Model model) {
        Post post = postRepository.findById(postId);
        List<Comment> comments = commentRepository.findByPostId(postId);
        model.addAttribute("post", post);
        model.addAttribute("comments", comments);
        return "post/detail";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("postSaveForm", new PostSaveForm());
        return "post/addForm";
    }

    @PostMapping("/add")
    public String save(@Validated @ModelAttribute PostSaveForm postSaveForm, BindingResult bindingResult, RedirectAttributes redirectAttributes) {

        if (bindingResult.hasErrors()) {
            return "post/addForm";
        }

        Post post = new Post(
                null,
                postSaveForm.getMemberId(),
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
