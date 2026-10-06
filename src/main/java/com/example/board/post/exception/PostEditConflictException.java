package com.example.board.post;

public class PostEditConflictException extends RuntimeException {
    public PostEditConflictException(String message) {
        super(message);
    }
}
