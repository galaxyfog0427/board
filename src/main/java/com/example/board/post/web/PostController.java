package com.example.board.post.web;

import com.example.board.comment.Comment;
import com.example.board.comment.CommentRepository;
import com.example.board.file.PostFile;
import com.example.board.file.PostFileRepository;
import com.example.board.global.web.VisitorCookieManager;
import com.example.board.file.FileStore;
import com.example.board.file.UploadFile;
import com.example.board.like.LikeStatus;
import com.example.board.like.PostLikeQueryService;
import com.example.board.login.MemberDetails;
import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.dto.PostListItem;
import com.example.board.post.exception.UnauthorizedPostAccessException;
import com.example.board.post.domain.Post;
import com.example.board.post.repository.PostRepository;
import com.example.board.post.service.PostService;
import com.example.board.ranking.PopularPostService;
import com.example.board.view.ViewCountService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.util.List;

@Controller
@RequestMapping("/posts")
public class PostController {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;
    private final MemberRepository memberRepository;
    private final PostFileRepository postFileRepository;
    private final FileStore fileStore;
    private final PostService postService;
    private final ViewCountService viewCountService;
    private final VisitorCookieManager visitorCookieManager;
    private final PostLikeQueryService postLikeQueryService;
    private final PopularPostService popularPostService;

    public PostController(PostRepository postRepository,
                          CommentRepository commentRepository,
                          MemberRepository memberRepository,
                          PostFileRepository postFileRepository,
                          FileStore fileStore,
                          PostService postService,
                          ViewCountService viewCountService,
                          VisitorCookieManager visitorCookieManager,
                          PostLikeQueryService postLikeQueryService,
                          PopularPostService popularPostService) {
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
        this.memberRepository = memberRepository;
        this.postFileRepository = postFileRepository;
        this.fileStore = fileStore;
        this.postService = postService;
        this.viewCountService = viewCountService;
        this.visitorCookieManager = visitorCookieManager;
        this.postLikeQueryService = postLikeQueryService;
        this.popularPostService = popularPostService;
    }

    @GetMapping
    public String list(@AuthenticationPrincipal MemberDetails memberDetails,
                       @PageableDefault(size = 10, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable,
                       Model model) {
        Page<PostListItem> posts = postRepository.findAllWithWriter(pageable);
        viewCountService.applyPendingView(posts.getContent());
        postLikeQueryService.applyLikeCounts(posts.getContent());
        model.addAttribute("posts", posts);
        model.addAttribute("popularPosts", popularPostService.getTop());
        model.addAttribute("loginMember", memberDetails);
        return "post/list";
    }

    @GetMapping("/{postId}")
    public String detail(@PathVariable("postId") Long postId,
                         @AuthenticationPrincipal MemberDetails memberDetails,
                         HttpServletRequest request,
                         HttpServletResponse response,
                         Model model) {
        Post post = postService.getPost(postId);
        Long memberId = memberDetails != null ? memberDetails.getMemberId() : null;

        if (memberId != null) {
            viewCountService.increaseForMember(postId, memberId);
        } else {
            viewCountService.increaseForVisitor(postId, visitorCookieManager.getOrIssue(request, response));
        }
        long viewCount = post.getViewCount() + viewCountService.getPendingCount(postId);
        LikeStatus likeStatus = postLikeQueryService.getStatus(postId, memberId);

        Member writer = memberRepository.findById(post.getMember().getId()).get();
        List<Comment> comments = commentRepository.findByPostId(postId);
        List<PostFile> postFiles = postFileRepository.findByPostId(postId);
        model.addAttribute("post", post);
        model.addAttribute("writer", writer);
        model.addAttribute("viewCount", viewCount);
        model.addAttribute("comments", comments);
        model.addAttribute("postFiles", postFiles);
        model.addAttribute("likeStatus", likeStatus);
        model.addAttribute("loginMember", memberDetails);
        return "post/detail";
    }

    @GetMapping("/add")
    public String addForm(Model model) {
        model.addAttribute("postSaveForm", new PostSaveForm());
        return "post/addForm";
    }

    @PostMapping("/add")
    public String save(@Validated @ModelAttribute PostSaveForm postSaveForm, BindingResult bindingResult,
                       @AuthenticationPrincipal MemberDetails memberDetails, RedirectAttributes redirectAttributes) throws IOException {

        if (bindingResult.hasErrors()) {
            return "post/addForm";
        }

        Post post = new Post(
                null,
                memberRepository.getReferenceById(memberDetails.getMemberId()),
                postSaveForm.getTitle(),
                postSaveForm.getContent(),
                null);
        Long savedPostId = postService.save(post);

        if (postSaveForm.getFiles() != null) {
            for (MultipartFile multipartFile : postSaveForm.getFiles()) {
                if (multipartFile.isEmpty()) {
                    continue;
                }
                UploadFile uploadFile = fileStore.storeFile(multipartFile);
                PostFile postFile = new PostFile(
                        null,
                        savedPostId,
                        uploadFile.getUploadFileName(),
                        uploadFile.getStoreFileName(),
                        multipartFile.getSize(),
                        null
                );
                postFileRepository.save(postFile);
            }
        }

        redirectAttributes.addAttribute("postId", savedPostId);
        return "redirect:/posts/{postId}";
    }

    @GetMapping("/{postId}/edit")
    public String editForm(@PathVariable("postId") Long postId,
                           @AuthenticationPrincipal MemberDetails memberDetails,
                           Model model) {
        Post post = postService.getPost(postId);

        if (!memberDetails.getMemberId().equals(post.getMember().getId())) {
            throw new UnauthorizedPostAccessException("본인이 작성한 게시글만 수정할 수 있습니다.");
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
                       @AuthenticationPrincipal MemberDetails memberDetails,
                       Model model) {

        Post post = postService.getPost(postId);

        if (!memberDetails.getMemberId().equals(post.getMember().getId())) {
            throw new UnauthorizedPostAccessException("본인이 작성한 게시글만 수정할 수 있습니다.");
        }

        if (bindingResult.hasErrors()) {
            model.addAttribute("postId", postId);
            return "post/editForm";
        }

        postService.editPost(postId, postEditForm.getTitle(), postEditForm.getContent());
        return "redirect:/posts/{postId}";
    }
}
