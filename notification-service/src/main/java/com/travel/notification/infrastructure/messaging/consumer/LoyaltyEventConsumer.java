package com.travel.notification.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.notification.application.service.NotificationDispatcher;
import com.travel.notification.domain.model.Notification;
import com.travel.notification.domain.model.NotificationType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * The consumer NotificationType.LOYALTY_POINTS_EARNED and
 * LOYALTY_TIER_UPGRADED have been missing since Day 9 — those enum
 * values and their EmailNotificationSender switch cases existed with
 * no producer to ever trigger them and no template file backing them
 * (fixed alongside this class — see loyalty-points-earned.html and
 * loyalty-tier-upgraded.html, this same commit).
 *
 * LoyaltyTierChangedEvent maps to LOYALTY_TIER_UPGRADED specifically —
 * see that event class's Javadoc (loyalty-service, Day 19) for why a
 * "changed" event is always an upgrade in this domain.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class LoyaltyEventConsumer {

    private final NotificationDispatcher dispatcher;
    private final ObjectMapper           objectMapper;

    @KafkaListener(topics = KafkaTopics.LOYALTY_POINTS_EARNED, groupId = "notification-service-group")
    public void onPointsEarned(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(payload);

            String userId       = node.get("userId").asText();
            long   pointsEarned = node.get("pointsEarned").asLong();
            long   newBalance   = node.get("newBalance").asLong();

            Notification notification = Notification.email(
                userId,
                userId + "@placeholder.com",
                NotificationType.LOYALTY_POINTS_EARNED,
                Map.of(
                    "fullName",     "Traveller",
                    "pointsEarned", String.valueOf(pointsEarned),
                    "newBalance",   String.valueOf(newBalance)
                ));

            dispatcher.dispatch(notification);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process LoyaltyPointsEarned notification: {}", ex.getMessage(), ex);
        }
    }

    @KafkaListener(topics = KafkaTopics.LOYALTY_TIER_CHANGED, groupId = "notification-service-group")
    public void onTierChanged(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(payload);

            String userId  = node.get("userId").asText();
            String newTier = node.get("newTier").asText();

            Notification notification = Notification.email(
                userId,
                userId + "@placeholder.com",
                NotificationType.LOYALTY_TIER_UPGRADED,
                Map.of(
                    "fullName", "Traveller",
                    "newTier",  newTier
                ));

            dispatcher.dispatch(notification);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process LoyaltyTierChanged notification: {}", ex.getMessage(), ex);
        }
    }
}
