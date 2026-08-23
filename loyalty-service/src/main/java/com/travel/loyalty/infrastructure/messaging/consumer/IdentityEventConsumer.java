package com.travel.loyalty.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.loyalty.application.usecase.CreateLoyaltyAccountUseCase;
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

    private final CreateLoyaltyAccountUseCase createAccountUseCase;
    private final ObjectMapper                objectMapper;

    @KafkaListener(topics = KafkaTopics.USER_REGISTERED, groupId = "loyalty-service-group")
    public void onUserRegistered(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node   = objectMapper.readTree(payload);
            String    userId = node.get("userId").asText();

            createAccountUseCase.execute(userId);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to provision loyalty account from UserRegistered event: {}", ex.getMessage(), ex);
        }
    }
}
