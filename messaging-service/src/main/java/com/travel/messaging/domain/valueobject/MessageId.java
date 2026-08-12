package com.travel.messaging.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class MessageId implements ValueObject {

    private final String value;

    private MessageId(String value) {
        this.value = Objects.requireNonNull(value, "MessageId must not be null");
    }

    public static MessageId generate()       { return new MessageId(UUID.randomUUID().toString()); }
    public static MessageId of(String value) { return new MessageId(value); }
    public String getValue()                  { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof MessageId m && Objects.equals(value, m.value);
    }
    @Override public int    hashCode()  { return Objects.hash(value); }
    @Override public String toString()  { return value; }
}
