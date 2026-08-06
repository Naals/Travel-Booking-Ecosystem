package com.travel.user.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.user.application.usecase.CreateUserProfileUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Reacts to identity-service's registration event by auto-provisioning
 * a profile. This is the ONLY way a UserProfile ever comes into
 * existence — see GetUserProfileUseCase's Javadoc for why a missing
 * profile surfaces as 404 rather than being lazily created elsewhere.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class IdentityEventConsumer {

    private final CreateUserProfileUseCase createProfileUseCase;
    private final ObjectMapper             objectMapper;

    @KafkaListener(topics = KafkaTopics.USER_REGISTERED, groupId = "user-service-group")
    public void onUserRegistered(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node     = objectMapper.readTree(payload);
            String    userId   = node.get("userId").asText();
            String    fullName = node.get("fullName").asText();

            createProfileUseCase.execute(userId, fullName);
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to create profile from UserRegistered event: {}", ex.getMessage(), ex);
        }
    }
}
