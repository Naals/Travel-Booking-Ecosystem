package com.travel.wallet.infrastructure.messaging.producer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.shared.event.DomainEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletEventPublisher {

    private final KafkaTemplate<String, String> kafkaTemplate;
    private final ObjectMapper                  objectMapper;

    public void publishEvents(List<DomainEvent> events) {
        for (DomainEvent event : events) {
            try {
                String topic = resolveTopic(event);
                if (topic == null) { log.warn("No topic for: {}", event.getEventType()); continue; }
                kafkaTemplate.send(topic, event.getAggregateId(), objectMapper.writeValueAsString(event));
                log.debug("Published {} → {}", event.getEventType(), topic);
            } catch (Exception ex) {
                log.error("Publish failed for {}: {}", event.getEventType(), ex.getMessage(), ex);
                throw new RuntimeException("Event publish failed", ex);
            }
        }
    }

    private String resolveTopic(DomainEvent event) {
        return switch (event.getEventType()) {
            case "WalletCreated"  -> KafkaTopics.WALLET_CREATED;
            case "WalletCredited" -> KafkaTopics.WALLET_CREDITED;
            case "WalletDebited"  -> KafkaTopics.WALLET_DEBITED;
            case "WalletFrozen"   -> KafkaTopics.WALLET_FROZEN;
            default -> null;
        };
    }
}
