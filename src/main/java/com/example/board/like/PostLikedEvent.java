package com.example.board.like;

import java.time.LocalDate;

public record PostLikedEvent(Long postId, Long memberId, LocalDate likedDate) {
}
