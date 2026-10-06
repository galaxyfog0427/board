package com.example.board.comment;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public class CommentSaveForm {

    @NotBlank
    @Size(max = 1000)
    private String content;

    public CommentSaveForm() {

    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
