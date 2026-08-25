package com.travel.fraud.domain;

import com.travel.common.exception.BusinessRuleViolationException;
import com.travel.fraud.domain.aggregate.RiskProfile;
import com.travel.fraud.domain.event.FraudAlertRaisedEvent;
import com.travel.fraud.domain.event.RiskFlagClearedEvent;
import com.travel.fraud.domain.valueobject.RiskProfileId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.*;

@DisplayName("RiskProfile aggregate")
class RiskProfileTest {

    static final RiskProfileId USER_ID = RiskProfileId.of("user-123");

    RiskProfile profile;

    @BeforeEach
    void setUp() {
        profile = RiskProfile.provision(USER_ID, Instant.now());
    }

    @Nested
    @DisplayName("Provisioning")
    class Provisioning {

        @Test @DisplayName("starts unflagged with zero counts")
        void startsClean() {
            assertThat(profile.isFlagged()).isFalse();
            var snap = profile.toSnapshot();
            assertThat(snap.recentBookingCount()).isZero();
            assertThat(snap.recentPaymentFailureCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Recording signals")
    class RecordingSignals {

        @Test @DisplayName("recentBookingCount reflects timestamps within the window")
        void bookingCountWithinWindow() {
            profile.recordBookingCreated(Instant.now());
            profile.recordBookingCreated(Instant.now());
            assertThat(profile.toSnapshot().recentBookingCount()).isEqualTo(2);
        }

        @Test @DisplayName("timestamps older than the window are excluded from the snapshot")
        void excludesOldTimestamps() {
            profile.recordBookingCreated(Instant.now().minus(java.time.Duration.ofHours(2)));
            assertThat(profile.toSnapshot().recentBookingCount()).isZero();
        }

        @Test @DisplayName("recordBookingCompleted increments lifetime count only")
        void completedIncrementsLifetimeOnly() {
            profile.recordBookingCompleted();
            profile.recordBookingCompleted();
            assertThat(profile.getLifetimeCompletedBookings()).isEqualTo(2L);
            assertThat(profile.toSnapshot().recentBookingCount()).isZero();
        }
    }

    @Nested
    @DisplayName("Raising an alert")
    class RaisingAlert {

        @Test @DisplayName("sets flagged and raises FraudAlertRaisedEvent")
        void raisesEvent() {
            profile.raiseAlert("RAPID_BOOKING_VELOCITY", "5 bookings in an hour");

            assertThat(profile.isFlagged()).isTrue();
            assertThat(profile.getFlagReason()).isEqualTo("5 bookings in an hour");
            assertThat(profile.getDomainEvents()).hasSize(1);
            assertThat(profile.getDomainEvents().get(0)).isInstanceOf(FraudAlertRaisedEvent.class);
        }

        @Test @DisplayName("is idempotent — a second call is a silent no-op")
        void idempotent() {
            profile.raiseAlert("RULE_A", "first reason");
            profile.clearDomainEvents();

            profile.raiseAlert("RULE_B", "second reason");

            assertThat(profile.getFlagReason()).isEqualTo("first reason"); // unchanged
            assertThat(profile.getDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Wallet-frozen reaction")
    class WalletFrozenReaction {

        @Test @DisplayName("marks flagged without raising a new event")
        void marksFlaggedSilently() {
            profile.onWalletFrozen("Staff-initiated freeze");

            assertThat(profile.isFlagged()).isTrue();
            assertThat(profile.getDomainEvents()).isEmpty();
        }

        @Test @DisplayName("does not overwrite an existing flag reason")
        void doesNotOverwriteReason() {
            profile.raiseAlert("RULE_A", "original reason");
            profile.clearDomainEvents();

            profile.onWalletFrozen("some other reason");

            assertThat(profile.getFlagReason()).isEqualTo("original reason");
        }
    }

    @Nested
    @DisplayName("Clearing the flag")
    class ClearingFlag {

        @Test @DisplayName("resets flagged state and both timestamp windows, raises RiskFlagClearedEvent")
        void clearsAndResets() {
            profile.recordBookingCreated(Instant.now());
            profile.raiseAlert("RULE_A", "reason");
            profile.clearDomainEvents();

            profile.clearFlag("staff-1");

            assertThat(profile.isFlagged()).isFalse();
            assertThat(profile.getFlagReason()).isNull();
            assertThat(profile.toSnapshot().recentBookingCount()).isZero();
            assertThat(profile.getDomainEvents().get(0)).isInstanceOf(RiskFlagClearedEvent.class);
        }

        @Test @DisplayName("cannot clear a flag that isn't set")
        void cannotClearUnflagged() {
            assertThatThrownBy(() -> profile.clearFlag("staff-1"))
                .isInstanceOf(BusinessRuleViolationException.class)
                .hasMessageContaining("not flagged");
        }
    }
}
