package com.example.board.comment;

import com.example.board.member.Member;
import com.example.board.member.MemberRepository;
import com.example.board.post.domain.Post;
import com.example.board.post.exception.PostNotFoundException;
import com.example.board.post.repository.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;
    private final MemberRepository memberRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository, MemberRepository memberRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
        this.memberRepository = memberRepository;
    }

    @Transactional
    public Long addComment(Comment comment) {
        Long commentId = commentRepository.save(comment).getId();
        validateForRollbackTest(comment);
        postRepository.incrementCommentCount(comment.getPost().getId());
        return commentId;
    }

    @Transactional
    public Long write(Long postId, Long memberId, String content) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));

        Member member = memberRepository.getReferenceById(memberId);
        Comment comment = new Comment(null, post, member, content);
        return addComment(comment);
    }

    // 트랜잭션 롤백 검증용
    private void validateForRollbackTest(Comment comment) {
        if (comment.getContent().equals("ROLLBACK_TEST")) {
            throw new IllegalStateException("의도적으로 발생시킨 예외 (롤백 테스트용)");
        }
    }

}
