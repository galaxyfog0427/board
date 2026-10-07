package com.example.board.like;

import java.time.LocalDate;

public record PostUnlikedEvent(Long postId, Long memberId, LocalDate likedDate) {
}
