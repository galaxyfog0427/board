package com.example.board.login;

import com.example.board.member.Member;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.*;

import static org.assertj.core.api.Assertions.*;

class MemberDetailsTest {

    @Test
    @DisplayName("세션에 저장될 수 있도록 직렬화 후 복원해도 같은 값을 가진다")
    void serializable() throws Exception {
        Member member = new Member(7L, "tester", "hashed", "테스터", null, null);
        MemberDetails original = new MemberDetails(member);

        MemberDetails restored = roundTrip(original);

        assertThat(restored.getMemberId()).isEqualTo(7L);
        assertThat(restored.getUsername()).isEqualTo("tester");
        assertThat(restored.getNickname()).isEqualTo("테스터");
    }

    @Test
    @DisplayName("eraseCredentials 이후에는 비밀번호가 남지 않는다")
    void eraseCredentials() {
        MemberDetails details = new MemberDetails(new Member(7L, "tester", "hashed", "테스터", null, null));

        details.eraseCredentials();

        assertThat(details.getPassword()).isNull();
    }

    private MemberDetails roundTrip(MemberDetails details) throws IOException, ClassNotFoundException {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        try (ObjectOutputStream out = new ObjectOutputStream(bytes)) {
            out.writeObject(details);
        }
        try (ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(bytes.toByteArray()))) {
            return (MemberDetails) in.readObject();
        }
    }

}