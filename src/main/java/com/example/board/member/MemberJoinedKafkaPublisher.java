package com.example.board.member;

import com.example.board.global.kafka.KafkaTopics;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import tools.jackson.databind.json.JsonMapper;

@Slf4j
@Component
public class MemberJoinedKafkaPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final JsonMapper jsonMapper;

    public MemberJoinedKafkaPublisher(KafkaTemplate<String, String> kafkaTemplate, JsonMapper jsonMapper) {
        this.kafkaTemplate = kafkaTemplate;
        this.jsonMapper = jsonMapper;
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void onMemberJoined(MemberJoinedEvent event) {
        String key = String.valueOf(event.memberId());
        String value = jsonMapper.writeValueAsString(event);

        kafkaTemplate.send(KafkaTopics.MEMBER_JOINED, key, value)
                .whenComplete((result, ex) -> {
                    if (ex != null) {
                        log.warn("[가입 메시지 발행 실패] memberId={}, cause={}", event.memberId(), ex.getMessage());
                        return;
                    }
                    log.info("[가입 메시지 발행] memberId={}, partition={}, offset={}",
                            event.memberId(),
                            result.getRecordMetadata().partition(),
                            result.getRecordMetadata().offset());
                });
    }
}
