package com.example.board.post;

public class UnauthorizedPostAccessException extends RuntimeException {

    public UnauthorizedPostAccessException(String message) {
        super(message);
    }
}
