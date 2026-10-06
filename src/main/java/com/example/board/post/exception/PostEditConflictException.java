package com.example.board.post.exception;

public class PostEditConflictException extends RuntimeException {
    public PostEditConflictException(String message) {
        super(message);
    }
}
