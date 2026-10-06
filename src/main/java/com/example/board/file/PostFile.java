package com.example.board.post;

import java.time.LocalDateTime;
import java.util.List;

public class PostFile {

    private static final List<String> IMAGE_EXTENSIONS =
            List.of("jpg", "jpeg", "png", "gif", "webp");

    private Long id;
    private Long postId;
    private String uploadFileName;
    private String storeFileName;
    private Long fileSize;
    private LocalDateTime createdAt;

    public PostFile(Long id, Long postId, String uploadFileName, String storeFileName, Long fileSize, LocalDateTime createdAt) {
        this.id = id;
        this.postId = postId;
        this.uploadFileName = uploadFileName;
        this.storeFileName = storeFileName;
        this.fileSize = fileSize;
        this.createdAt = createdAt;
    }

    public boolean isImage() {
        int pos = uploadFileName.lastIndexOf(".");
        if (pos == -1) {
            return false;
        }
        String ext = uploadFileName.substring(pos + 1).toLowerCase();
        return IMAGE_EXTENSIONS.contains(ext);
    }

    public Long getId() {
        return id;
    }

    public Long getPostId() {
        return postId;
    }

    public String getUploadFileName() {
        return uploadFileName;
    }

    public String getStoreFileName() {
        return storeFileName;
    }

    public Long getFileSize() {
        return fileSize;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
