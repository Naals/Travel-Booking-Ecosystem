package com.travel.analytics.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.analytics.application.usecase.RecordPaymentCompletedUseCase;
import com.travel.analytics.application.usecase.RecordRefundCompletedUseCase;
import com.travel.analytics.domain.repository.EventDeduplicationRepository;
import com.travel.common.event.KafkaTopics;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;

/** Same handle()/dedup/date-bucketing shape as BookingMetricsConsumer — see that class's Javadoc. */
@Slf4j
@Component
@RequiredArgsConstructor
public class RevenueMetricsConsumer {

    private final RecordPaymentCompletedUseCase paymentCompletedUseCase;
    private final RecordRefundCompletedUseCase  refundCompletedUseCase;
    private final EventDeduplicationRepository  deduplicationRepository;
    private final ObjectMapper                   objectMapper;

    @KafkaListener(topics = KafkaTopics.PAYMENT_COMPLETED, groupId = "analytics-service-group")
    @Transactional
    public void onPaymentCompleted(@Payload String payload, Acknowledgment ack) {
        handle(payload, "PaymentCompleted", ack, node -> {
            BigDecimal amount   = new BigDecimal(node.get("amount").get("amount").asText());
            String     currency = node.get("amount").get("currency").asText();
            paymentCompletedUseCase.execute(amount, currency, dateOf(node));
        });
    }

    @KafkaListener(topics = KafkaTopics.REFUND_COMPLETED, groupId = "analytics-service-group")
    @Transactional
    public void onRefundCompleted(@Payload String payload, Acknowledgment ack) {
        handle(payload, "RefundCompleted", ack, node -> {
            BigDecimal amount   = new BigDecimal(node.get("amount").get("amount").asText());
            String     currency = node.get("amount").get("currency").asText();
            refundCompletedUseCase.execute(amount, currency, dateOf(node));
        });
    }

    private LocalDate dateOf(JsonNode node) {
        return Instant.parse(node.get("occurredOn").asText()).atZone(ZoneOffset.UTC).toLocalDate();
    }

    private void handle(String payload, String eventType, Acknowledgment ack, ConsumerStep step) {
        try {
            JsonNode node    = objectMapper.readTree(payload);
            String    eventId = node.get("eventId").asText();

            if (!deduplicationRepository.markProcessedIfNew(eventId)) {
                log.debug("Duplicate {} (eventId={}) — already processed, skipping", eventType, eventId);
                ack.acknowledge();
                return;
            }

            step.execute(node);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process {}: {}", eventType, ex.getMessage(), ex);
        }
    }

    @FunctionalInterface
    interface ConsumerStep {
        void execute(JsonNode node) throws Exception;
    }
}
