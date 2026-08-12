package com.travel.messaging.domain.valueobject;

import com.travel.shared.domain.ValueObject;
import java.util.Objects;
import java.util.UUID;

public final class ConversationId implements ValueObject {

    private final String value;

    private ConversationId(String value) {
        this.value = Objects.requireNonNull(value, "ConversationId must not be null");
    }

    public static ConversationId generate()       { return new ConversationId(UUID.randomUUID().toString()); }
    public static ConversationId of(String value) { return new ConversationId(value); }
    public String getValue()                       { return value; }

    @Override public boolean equals(Object o) {
        return o instanceof ConversationId c && Objects.equals(value, c.value);
    }
    @Override public int    hashCode()  { return Objects.hash(value); }
    @Override public String toString()  { return value; }
}
