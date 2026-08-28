package com.travel.audit.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

/**
 * Always freshly generated — unlike UserId/WalletId/LoyaltyAccountId/
 * RiskProfileId (Days 15, 18, 19, 21), which deliberately have no
 * .generate() because they must equal identity-service's userId, an
 * audit entry has no natural external identity to inherit. It follows
 * the same convention as BookingId, PaymentId, and ReviewId instead.
 */
public final class AuditLogId implements ValueObject {

    private final String value;

    private AuditLogId(String value) {
        this.value = Objects.requireNonNull(value, "AuditLogId must not be null");
    }

    public static AuditLogId generate()       { return new AuditLogId(UUID.randomUUID().toString()); }
    public static AuditLogId of(String value) { return new AuditLogId(value); }
    public String getValue()                    { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof AuditLogId a && Objects.equals(value, a.value);
    }
    @Override public int    hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
