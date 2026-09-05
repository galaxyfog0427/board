package com.example.board.api;

import java.time.LocalDateTime;
import java.util.List;

public record PostDetailResponse(
        Long id,
        String title,
        String content,
        String writerNickname,
        Integer commentCount,
        LocalDateTime createdAt,
        List<CommentResponse> comments
) {
}
