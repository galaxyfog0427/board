package com.example.board.post;

public class PostForm {

    //TODO: 로그인 기능 (MVC2 6장) 구현 후 세선에서 자동으로 채우도록 변경 예정
    private Long memberId;
    private String title;
    private String content;

    public PostForm() {}

    public Long getMemberId() {
        return memberId;
    }

    public void setMemberId(Long memberId) {
        this.memberId = memberId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
