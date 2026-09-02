package com.example.board.member;

import com.example.board.common.BaseTimeEntity;
import jakarta.persistence.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "member")
public class Member extends BaseTimeEntity {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "member_id")
    private Long id;

    private String loginId;
    private String password;
    private String nickname;

    @Enumerated(EnumType.STRING)
    private MemberStatus status;

    private LocalDateTime withdrawnAt;

    protected Member() {
    }

    public Member(Long id, String loginId, String password, String nickname, MemberStatus status, LocalDateTime withdrawnAt) {
        this.id = id;
        this.loginId = loginId;
        this.password = password;
        this.nickname = nickname;
        this.status = status;
        this.withdrawnAt = withdrawnAt;
    }

    @PrePersist
    void prePersist() {
        if (this.status == null) {
            this.status = MemberStatus.ACTIVE;
        }
    }

    public Long getId() {
        return id;
    }

    public String getLoginId() {
        return loginId;
    }

    public String getPassword() {
        return password;
    }

    public String getNickname() {
        return nickname;
    }

    public MemberStatus getStatus() {
        return status;
    }

    public LocalDateTime getWithdrawnAt() {
        return withdrawnAt;
    }

    @Override
    public String toString() {
        return "Member{" +
                "id=" + id +
                ", loginId='" + loginId + '\'' +
                ", nickname='" + nickname + '\'' +
                ", status=" + status +
                '}';
    }
}
