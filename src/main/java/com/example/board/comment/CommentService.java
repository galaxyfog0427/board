package com.example.board.comment;

import com.example.board.post.PostRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;

@Service
public class CommentService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    public CommentService(CommentRepository commentRepository, PostRepository postRepository) {
        this.commentRepository = commentRepository;
        this.postRepository = postRepository;
    }

    @Transactional
    public Long addComment(Comment comment) throws SQLException {
        Long commentId = commentRepository.save(comment);
        validateForRollbackTest(comment);
        postRepository.incrementCommentCount(comment.getPostId());
        return commentId;
    }

    // 트랜잭션 롤백 검증용
    private void validateForRollbackTest(Comment comment) {
        if (comment.getContent().equals("ROLLBACK_TEST")) {
            throw new IllegalStateException("의도적으로 발생시킨 예외 (롤백 테스트용)");
        }
    }

}
