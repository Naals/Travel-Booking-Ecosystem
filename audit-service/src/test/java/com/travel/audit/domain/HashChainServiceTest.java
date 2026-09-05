package com.travel.audit.domain;

import com.travel.audit.domain.model.AuditCategory;
import com.travel.audit.domain.service.HashChainService;
import com.travel.audit.domain.valueobject.ChainHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("HashChainService")
class HashChainServiceTest {

    static final Instant OCCURRED_AT = Instant.parse("2026-08-25T10:00:00Z");

    @Test @DisplayName("is deterministic — identical inputs produce identical hashes")
    void deterministic() {
        ChainHash a = HashChainService.computeHash(
            1L, AuditCategory.BOOKING, "BookingCreated", "evt-1",
            "booking-1", "Booking created", OCCURRED_AT, ChainHash.GENESIS);
        ChainHash b = HashChainService.computeHash(
            1L, AuditCategory.BOOKING, "BookingCreated", "evt-1",
            "booking-1", "Booking created", OCCURRED_AT, ChainHash.GENESIS);
        assertThat(a).isEqualTo(b);
    }

    @Test @DisplayName("changing the summary changes the hash")
    void summaryAffectsHash() {
        ChainHash original = HashChainService.computeHash(
            1L, AuditCategory.BOOKING, "BookingCreated", "evt-1",
            "booking-1", "Original summary", OCCURRED_AT, ChainHash.GENESIS);
        ChainHash altered = HashChainService.computeHash(
            1L, AuditCategory.BOOKING, "BookingCreated", "evt-1",
            "booking-1", "Altered summary", OCCURRED_AT, ChainHash.GENESIS);
        assertThat(original).isNotEqualTo(altered);
    }

    @Test @DisplayName("changing the previous hash changes the resulting hash")
    void previousHashAffectsResult() {
        ChainHash withGenesis = HashChainService.computeHash(
            2L, AuditCategory.PAYMENT, "PaymentCompleted", "evt-2",
            "payment-1", "Payment completed", OCCURRED_AT, ChainHash.GENESIS);
        ChainHash withOther = HashChainService.computeHash(
            2L, AuditCategory.PAYMENT, "PaymentCompleted", "evt-2",
            "payment-1", "Payment completed", OCCURRED_AT, ChainHash.of("f".repeat(64)));
        assertThat(withGenesis).isNotEqualTo(withOther);
    }

    @Test @DisplayName("always produces a valid 64-character hex ChainHash")
    void producesValidFormat() {
        ChainHash hash = HashChainService.computeHash(
            1L, AuditCategory.FRAUD, "FraudAlertRaised", "evt-3",
            "user-1", "Fraud alert raised", OCCURRED_AT, ChainHash.GENESIS);
        assertThat(hash.getValue()).hasSize(64).matches("[0-9a-f]{64}");
    }
}
