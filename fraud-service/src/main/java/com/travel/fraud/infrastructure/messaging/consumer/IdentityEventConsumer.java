package com.travel.fraud.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.fraud.application.usecase.CreateRiskProfileUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityEventConsumer {

    private final CreateRiskProfileUseCase createProfileUseCase;
    private final ObjectMapper             objectMapper;

    @KafkaListener(topics = KafkaTopics.USER_REGISTERED, groupId = "fraud-service-group")
    public void onUserRegistered(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node   = objectMapper.readTree(payload);
            String    userId = node.get("userId").asText();
            createProfileUseCase.execute(userId);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to provision risk profile from UserRegistered event: {}", ex.getMessage(), ex);
        }
    }
}
