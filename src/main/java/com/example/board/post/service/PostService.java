package com.example.board.post.service;

import com.example.board.file.PostFile;
import com.example.board.file.PostFileRepository;
import com.example.board.file.UploadFile;
import com.example.board.post.domain.Post;
import com.example.board.post.exception.PostEditConflictException;
import com.example.board.post.exception.PostNotFoundException;
import com.example.board.post.repository.PostRepository;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class PostService {

    private final PostRepository postRepository;
    private final PostFileRepository postFileRepository;

    public PostService(PostRepository postRepository, PostFileRepository postFileRepository) {
        this.postRepository = postRepository;
        this.postFileRepository = postFileRepository;
    }

    @Transactional(readOnly = true)
    public Post getPost(Long postId) {
        return postRepository.findByIdWithMember(postId)
                .orElseThrow(() -> new PostNotFoundException("존재하지 않는 게시글입니다."));
    }

    @Transactional
    public void editPost(Long postId, String title, String content) {
        Post post = getPost(postId);
        post.changeTitleAndContent(title, content);
        try {
            postRepository.flush();
        } catch (ObjectOptimisticLockingFailureException e) {
            throw new PostEditConflictException("다른 사용자가 이미 이 게시글을 수정했습니다. 새로고침 후 다시 시도해주세요.");
        }
    }

    @Transactional
    public Long saveWithFiles(Post post, List<UploadFile> uploadFiles) {
        Long postId = postRepository.save(post).getId();
        for (UploadFile uploadFile : uploadFiles) {
            postFileRepository.save(new PostFile(
                    null,
                    postId,
                    uploadFile.getUploadFileName(),
                    uploadFile.getStoreFileName(),
                    uploadFile.getFileSize(),
                    null));
        }

        return postId;
    }

}
