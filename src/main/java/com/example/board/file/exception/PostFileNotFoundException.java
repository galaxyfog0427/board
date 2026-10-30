package com.example.board.file.exception;

public class PostFileNotFoundException extends RuntimeException {
    public PostFileNotFoundException(Long fileId) {
        super("첨부파일이 없습니다: fileId = " + fileId);
    }
}
