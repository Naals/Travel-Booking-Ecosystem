package com.travel.user.domain.model;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;

/**
 * User profile identifier.
 *
 * Deliberately has no .generate() factory, unlike every other ID
 * value object in this platform (BookingId, PaymentId, etc.). A
 * UserProfile's identity is always assigned externally — it equals
 * the userId minted by identity-service at registration (see
 * UserRegisteredEvent.getUserId()). This keeps the two bounded
 * contexts' identities aligned without sharing a Java type across
 * service boundaries.
 */
public final class UserId implements ValueObject {

    private final String value;

    private UserId(String value) {
        this.value = Objects.requireNonNull(value, "UserId must not be null");
    }

    public static UserId of(String value) { return new UserId(value); }

    public String getValue() { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof UserId u && Objects.equals(value, u.value);
    }
    @Override public int    hashCode() { return Objects.hash(value); }
    @Override public String toString() { return value; }
}
