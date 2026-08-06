package com.travel.review.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class ReviewId implements ValueObject {

    private final String value;

    private ReviewId(String value) {
        this.value = Objects.requireNonNull(value, "ReviewId must not be null");
    }

    public static ReviewId generate()       { return new ReviewId(UUID.randomUUID().toString()); }
    public static ReviewId of(String value) { return new ReviewId(value); }
    public String getValue()                 { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof ReviewId r && Objects.equals(value, r.value);
    }
    @Override public int    hashCode()  { return Objects.hash(value); }
    @Override public String toString()  { return value; }
}
