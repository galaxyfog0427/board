package com.example.board.post;

import com.example.board.comment.Comment;
import com.example.board.comment.CommentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
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
        model.addAttribute("postForm", new PostForm());
        return "post/addForm";
    }

    @PostMapping("/add")
    public String save(@ModelAttribute PostForm postForm, RedirectAttributes redirectAttributes) {
        Post post = new Post(
                null,
                postForm.getMemberId(),
                postForm.getTitle(),
                postForm.getContent(),
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

        PostForm postForm = new PostForm();
        postForm.setMemberId(post.getMemberId());
        postForm.setTitle(post.getTitle());
        postForm.setContent(post.getContent());

        model.addAttribute("postForm", postForm);
        model.addAttribute("postId", postId);
        return "post/editForm";
    }

    @PostMapping("/{postId}/edit")
    public String edit(@PathVariable("postId") Long postId, @ModelAttribute PostForm postForm) {
        postRepository.update(postId, postForm.getTitle(), postForm.getContent());
        return "redirect:/posts/{postId}";
    }
}
