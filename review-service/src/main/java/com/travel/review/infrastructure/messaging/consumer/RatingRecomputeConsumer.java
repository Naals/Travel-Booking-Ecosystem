package com.travel.review.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.review.domain.model.ReviewedResourceType;
import com.travel.review.domain.service.RatingAggregationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Consumes review-service's own ResourceRatingUpdatedEvent to trigger
 * the actual recompute — a service consuming its own published topic.
 *
 * Decoupling the signal (raised synchronously inside the Review
 * aggregate on write/approve/reject) from the recompute work (a full
 * re-scan of that resource's approved reviews) means a slow recompute
 * never blocks the review-creation or moderation HTTP request itself,
 * and Kafka's redelivery-on-failure gives the recompute a free retry
 * mechanism without any bespoke retry logic here.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class RatingRecomputeConsumer {

    private final RatingAggregationService aggregationService;
    private final ObjectMapper             objectMapper;

    @KafkaListener(topics = KafkaTopics.RESOURCE_RATING_UPDATED, groupId = "review-service-group")
    public void onResourceRatingUpdated(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node = objectMapper.readTree(payload);
            String resourceId   = node.get("resourceId").asText();
            String resourceType = node.get("resourceType").asText();

            aggregationService.recompute(resourceId, ReviewedResourceType.valueOf(resourceType));
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to recompute rating: {}", ex.getMessage(), ex);
        }
    }
}
