package com.example.board.api;

import com.example.board.comment.Comment;
import com.example.board.comment.CommentRepository;
import com.example.board.common.ApiResponse;
import com.example.board.common.PageResponse;
import com.example.board.post.Post;
import com.example.board.post.PostListItem;
import com.example.board.post.PostRepository;
import com.example.board.post.PostService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/v1/posts")
public class PostApiController {

    private final PostService postService;
    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    public PostApiController(PostService postService, PostRepository postRepository, CommentRepository commentRepository) {
        this.postService = postService;
        this.postRepository = postRepository;
        this.commentRepository = commentRepository;
    }

    @GetMapping
    public ResponseEntity<ApiResponse<PageResponse<PostListItem>>> list(
            @PageableDefault(size = 10, sort = {"createdAt", "id"}, direction = Sort.Direction.DESC) Pageable pageable) {
        Page<PostListItem> posts = postRepository.findAllWithWriter(pageable);
        return ResponseEntity.ok(ApiResponse.success(PageResponse.from(posts)));
    }

    @GetMapping("/{postId}")
    public ResponseEntity<ApiResponse<PostDetailResponse>> detail(@PathVariable("postId") Long postId) {
        Post post = postService.getPost(postId);
        List<Comment> comments = commentRepository.findByPostId(postId);

        List<CommentResponse> commentResponses = comments.stream()
                .map(c -> new CommentResponse(c.getId(), c.getContent(), c.getMember().getNickname(), c.getCreatedAt()))
                .toList();

        PostDetailResponse response = new PostDetailResponse(
                post.getId(),
                post.getTitle(),
                post.getContent(),
                post.getMember().getNickname(),
                post.getCommentCount(),
                post.getCreatedAt(),
                commentResponses
        );

        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
