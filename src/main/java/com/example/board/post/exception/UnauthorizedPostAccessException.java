package com.example.board.post.exception;

public class UnauthorizedPostAccessException extends RuntimeException {

    public UnauthorizedPostAccessException(String message) {
        super(message);
    }
}
