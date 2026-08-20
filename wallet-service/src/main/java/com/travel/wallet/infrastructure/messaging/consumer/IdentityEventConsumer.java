package com.travel.wallet.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.wallet.application.usecase.CreateWalletUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * The only Kafka consumer in this service — see WalletServiceApplication's
 * Javadoc for why wallet-service doesn't yet consume anything related
 * to payments or refunds.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityEventConsumer {

    private final CreateWalletUseCase createWalletUseCase;
    private final ObjectMapper        objectMapper;

    @KafkaListener(topics = KafkaTopics.USER_REGISTERED, groupId = "wallet-service-group")
    public void onUserRegistered(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node   = objectMapper.readTree(payload);
            String    userId = node.get("userId").asText();

            createWalletUseCase.execute(userId);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to provision wallet from UserRegistered event: {}", ex.getMessage(), ex);
        }
    }
}
