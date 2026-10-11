package com.example.board.notification;

import com.example.board.global.kafka.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
public class WelcomeMailConsumer {

    private final JsonMapper jsonMapper;

    public WelcomeMailConsumer(JsonMapper jsonMapper) {
        this.jsonMapper = jsonMapper;
    }

    @KafkaListener(
            topics = KafkaTopics.MEMBER_JOINED,
            groupId = "welcome-mail-group"
    )
    public void consume(String message) {
        MemberJoinedMessage joined = jsonMapper.readValue(message, MemberJoinedMessage.class);

        log.info("[환영 메일 발송 시작] memberId={}", joined.memberId());
        sendWelcomeMail(joined);
        log.info("[환영 메일 발송 완료] memberId={}, nickname={}", joined.memberId(), joined.nickname());
    }

    private void sendWelcomeMail(MemberJoinedMessage joined) {
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
