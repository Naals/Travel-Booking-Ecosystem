package com.travel.fraud.domain;

import com.travel.fraud.domain.model.RiskSnapshot;
import com.travel.fraud.domain.service.*;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.*;

@DisplayName("FraudRuleEngine")
class FraudRuleEngineTest {

    FraudRuleEngine engine = new FraudRuleEngine(List.of(
        new RapidBookingVelocityRule(),
        new PaymentFailureVelocityRule(),
        new NewAccountRapidBookingRule()
    ));

    @Nested
    @DisplayName("No rule triggered")
    class NoTrigger {

        @Test @DisplayName("clean snapshot triggers nothing")
        void clean() {
            var snapshot = new RiskSnapshot(
                Instant.now().minusSeconds(86_400), 1, 0, 5, false);
            assertThat(engine.evaluate(snapshot)).isEmpty();
        }

        @Test @DisplayName("already-flagged snapshot short-circuits regardless of counts")
        void alreadyFlaggedShortCircuits() {
            var snapshot = new RiskSnapshot(
                Instant.now().minusSeconds(86_400), 999, 999, 0, true);
            assertThat(engine.evaluate(snapshot)).isEmpty();
        }
    }

    @Nested
    @DisplayName("Rule triggered")
    class Triggered {

        @Test @DisplayName("rapid booking velocity triggers with correct rule name")
        void rapidBooking() {
            var snapshot = new RiskSnapshot(
                Instant.now().minusSeconds(86_400), 5, 0, 0, false);
            var result = engine.evaluate(snapshot);
            assertThat(result).isPresent();
            assertThat(result.get().ruleName()).isEqualTo("RAPID_BOOKING_VELOCITY");
        }

        @Test @DisplayName("payment failure velocity triggers with correct rule name")
        void paymentFailures() {
            var snapshot = new RiskSnapshot(
                Instant.now().minusSeconds(86_400), 0, 3, 0, false);
            var result = engine.evaluate(snapshot);
            assertThat(result).isPresent();
            assertThat(result.get().ruleName()).isEqualTo("PAYMENT_FAILURE_VELOCITY");
        }

        @Test @DisplayName("new account rapid booking triggers only when both conditions hold")
        void newAccountRapidBooking() {
            var snapshot = new RiskSnapshot(
                Instant.now().minusSeconds(60), 3, 0, 0, false); // 1 min old, 3 bookings
            var result = engine.evaluate(snapshot);
            assertThat(result).isPresent();
            assertThat(result.get().ruleName()).isEqualTo("NEW_ACCOUNT_RAPID_BOOKING");
        }

        @Test @DisplayName("an old account with the same booking count does not trigger the new-account rule")
        void oldAccountDoesNotTriggerNewAccountRule() {
            var snapshot = new RiskSnapshot(
                Instant.now().minusSeconds(86_400), 3, 0, 0, false);
            // 3 bookings alone is below RapidBookingVelocityRule's threshold of 5
            assertThat(engine.evaluate(snapshot)).isEmpty();
        }
    }
}
