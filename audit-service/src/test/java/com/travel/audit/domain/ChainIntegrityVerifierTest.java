package com.travel.audit.domain;

import com.travel.audit.domain.model.AuditCategory;
import com.travel.audit.domain.model.AuditLogEntry;
import com.travel.audit.domain.service.ChainIntegrityVerifier;
import com.travel.audit.domain.service.HashChainService;
import com.travel.audit.domain.valueobject.AuditLogId;
import com.travel.audit.domain.valueobject.ChainHash;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("ChainIntegrityVerifier")
class ChainIntegrityVerifierTest {

    static final Instant T1 = Instant.parse("2026-08-25T10:00:00Z");
    static final Instant T2 = Instant.parse("2026-08-25T10:05:00Z");
    static final Instant T3 = Instant.parse("2026-08-25T10:10:00Z");

    /** Builds a genuine, correctly-linked chain — mirroring exactly what RecordAuditEntryUseCase produces. */
    private List<AuditLogEntry> buildValidChain() {
        ChainHash hash1 = HashChainService.computeHash(
            1L, AuditCategory.IDENTITY, "UserRegistered", "evt-1",
            "user-1", "User registered", T1, ChainHash.GENESIS);
        var entry1 = AuditLogEntry.create(AuditLogId.generate(), 1L, AuditCategory.IDENTITY,
            "UserRegistered", "evt-1", "user-1", "user-1", "User registered",
            ChainHash.GENESIS, hash1, T1, T1);

        ChainHash hash2 = HashChainService.computeHash(
            2L, AuditCategory.BOOKING, "BookingCreated", "evt-2",
            "booking-1", "Booking created", T2, hash1);
        var entry2 = AuditLogEntry.create(AuditLogId.generate(), 2L, AuditCategory.BOOKING,
            "BookingCreated", "evt-2", "booking-1", "user-1", "Booking created",
            hash1, hash2, T2, T2);

        ChainHash hash3 = HashChainService.computeHash(
            3L, AuditCategory.PAYMENT, "PaymentCompleted", "evt-3",
            "payment-1", "Payment completed", T3, hash2);
        var entry3 = AuditLogEntry.create(AuditLogId.generate(), 3L, AuditCategory.PAYMENT,
            "PaymentCompleted", "evt-3", "payment-1", "user-1", "Payment completed",
            hash2, hash3, T3, T3);

        return List.of(entry1, entry2, entry3);
    }

    @Nested
    @DisplayName("Valid chains")
    class ValidChains {

        @Test @DisplayName("an empty chain is trivially valid")
        void emptyChainIsValid() {
            var report = ChainIntegrityVerifier.verify(List.of());
            assertThat(report.valid()).isTrue();
            assertThat(report.entriesChecked()).isZero();
        }

        @Test @DisplayName("a correctly linked chain passes verification")
        void correctChainPasses() {
            var report = ChainIntegrityVerifier.verify(buildValidChain());
            assertThat(report.valid()).isTrue();
            assertThat(report.entriesChecked()).isEqualTo(3L);
            assertThat(report.brokenAtSequence()).isNull();
        }
    }

    @Nested
    @DisplayName("Tampered chains")
    class TamperedChains {

        @Test
        @DisplayName("detects a content change even when the stored contentHash was left unchanged")
        void detectsContentTampering() {
            var chain = buildValidChain();

            // Simulate someone editing entry #2's summary directly in
            // the database, without recomputing its contentHash — the
            // exact "quietly edit a row" tampering scenario this whole
            // feature exists to catch.
            var original = chain.get(1);
            var tampered = AuditLogEntry.reconstitute(
                original.getId(), original.getSequenceNumber(), original.getCategory(),
                original.getSourceEventType(), original.getSourceEventId(), original.getSubjectId(),
                original.getUserId(), "TAMPERED SUMMARY", original.getPreviousHash(),
                original.getContentHash(), // unchanged — the giveaway
                original.getOccurredAt(), original.getRecordedAt());

            var report = ChainIntegrityVerifier.verify(List.of(chain.get(0), tampered, chain.get(2)));

            assertThat(report.valid()).isFalse();
            assertThat(report.brokenAtSequence()).isEqualTo(2L);
            assertThat(report.failureReason()).contains("contentHash does not match");
        }

        @Test
        @DisplayName("detects a broken link when an entry's previousHash doesn't match its predecessor")
        void detectsBrokenLink() {
            var chain = buildValidChain();

            var original = chain.get(2);
            var relinked = AuditLogEntry.reconstitute(
                original.getId(), original.getSequenceNumber(), original.getCategory(),
                original.getSourceEventType(), original.getSourceEventId(), original.getSubjectId(),
                original.getUserId(), original.getSummary(),
                ChainHash.of("9".repeat(64)), // wrong previousHash — doesn't match entry #2's actual hash
                original.getContentHash(), original.getOccurredAt(), original.getRecordedAt());

            var report = ChainIntegrityVerifier.verify(List.of(chain.get(0), chain.get(1), relinked));

            assertThat(report.valid()).isFalse();
            assertThat(report.brokenAtSequence()).isEqualTo(3L);
            assertThat(report.failureReason()).contains("previousHash does not match");
        }

        @Test
        @DisplayName("a break at entry N reports entriesChecked as N-1 — the count that passed before failure")
        void reportsCorrectPassedCount() {
            var chain = buildValidChain();
            var tampered = AuditLogEntry.reconstitute(
                chain.get(1).getId(), chain.get(1).getSequenceNumber(), chain.get(1).getCategory(),
                chain.get(1).getSourceEventType(), chain.get(1).getSourceEventId(), chain.get(1).getSubjectId(),
                chain.get(1).getUserId(), "ALTERED", chain.get(1).getPreviousHash(),
                chain.get(1).getContentHash(), chain.get(1).getOccurredAt(), chain.get(1).getRecordedAt());

            var report = ChainIntegrityVerifier.verify(List.of(chain.get(0), tampered, chain.get(2)));
            assertThat(report.entriesChecked()).isEqualTo(1L); // only entry #1 verified clean before the break
        }
    }
}
