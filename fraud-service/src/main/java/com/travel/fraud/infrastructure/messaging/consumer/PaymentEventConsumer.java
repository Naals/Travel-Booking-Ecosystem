package com.travel.fraud.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.fraud.application.usecase.RecordPaymentFailedUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentEventConsumer {

    private final RecordPaymentFailedUseCase recordUseCase;
    private final ObjectMapper                objectMapper;

    @KafkaListener(topics = KafkaTopics.PAYMENT_FAILED, groupId = "fraud-service-group")
    public void onPaymentFailed(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node   = objectMapper.readTree(payload);
            String    userId = node.get("userId").asText();
            recordUseCase.execute(userId, Instant.now());
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to record PaymentFailed for fraud evaluation: {}", ex.getMessage(), ex);
        }
    }
}
