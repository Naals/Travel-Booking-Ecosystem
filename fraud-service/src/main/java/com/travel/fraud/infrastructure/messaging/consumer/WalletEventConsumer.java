package com.travel.fraud.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.fraud.application.usecase.HandleWalletFrozenUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/** The consumer WalletFrozenEvent's Day-18 Javadoc said didn't exist yet. */
@Slf4j
@Component
@RequiredArgsConstructor
public class WalletEventConsumer {

    private final HandleWalletFrozenUseCase useCase;
    private final ObjectMapper               objectMapper;

    @KafkaListener(topics = KafkaTopics.WALLET_FROZEN, groupId = "fraud-service-group")
    public void onWalletFrozen(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node   = objectMapper.readTree(payload);
            String    userId = node.get("userId").asText();
            String    reason  = node.path("reason").asText("Wallet frozen");
            useCase.execute(userId, reason);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process WalletFrozen for risk profile: {}", ex.getMessage(), ex);
        }
    }
}
