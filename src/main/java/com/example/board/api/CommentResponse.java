package com.example.board.api;

import java.time.LocalDateTime;

public record CommentResponse(
        Long id,
        String content,
        String writerNickname,
        LocalDateTime createdAt
) {
}
