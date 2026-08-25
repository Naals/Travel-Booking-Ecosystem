package com.travel.wallet.infrastructure.messaging.consumer;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.travel.common.event.KafkaTopics;
import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.common.exception.ResourceNotFoundException;
import com.travel.wallet.application.dto.request.FreezeWalletRequest;
import com.travel.wallet.application.usecase.FreezeWalletUseCase;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.support.Acknowledgment;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Component;

/**
 * Closes the loop fraud-service opened today: an alert automatically
 * freezes the wallet, reusing FreezeWalletUseCase (Day 18) exactly as
 * a staff-triggered freeze would — same aggregate method, same
 * WalletFrozenEvent, same downstream consumer in fraud-service (this
 * day's WalletEventConsumer). See ADR-013 for the full loop diagram.
 *
 * Two outcomes are treated as benign no-ops rather than errors: no
 * wallet exists yet for this user (nothing to freeze), or the wallet
 * is already frozen (idempotent — a second alert for an already-frozen
 * user is expected once RiskProfile.raiseAlert() has fired once, but
 * a race between two rules triggering in quick succession is still
 * possible before the first alert's local flagged=true has round-
 * tripped back through WALLET_FROZEN).
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class FraudEventConsumer {

    private final FreezeWalletUseCase freezeUseCase;
    private final ObjectMapper        objectMapper;

    @KafkaListener(topics = KafkaTopics.FRAUD_ALERT_RAISED, groupId = "wallet-service-group")
    public void onFraudAlertRaised(@Payload String payload, Acknowledgment ack) {
        try {
            JsonNode node     = objectMapper.readTree(payload);
            String    userId   = node.get("userId").asText();
            String    ruleName = node.get("ruleName").asText();
            String    reason    = node.get("reason").asText();

            freezeUseCase.execute(userId, new FreezeWalletRequest(
                "Automated fraud alert (" + ruleName + "): " + reason));

            log.warn("Wallet auto-frozen due to fraud alert: user={} rule={}", userId, ruleName);
            ack.acknowledge();

        } catch (ResourceNotFoundException ex) {
            log.warn("Fraud alert for user with no wallet — nothing to freeze: {}", ex.getMessage());
            ack.acknowledge();
        } catch (BusinessRuleViolationException ex) {
            log.info("Wallet already frozen — fraud alert is a no-op: {}", ex.getMessage());
            ack.acknowledge();
        } catch (Exception ex) {
            log.error("Failed to process FraudAlertRaised: {}", ex.getMessage(), ex);
        }
    }
}
