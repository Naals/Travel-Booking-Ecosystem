package com.travel.testsupport;

import com.fasterxml.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Builds a JSON payload shaped like a real DomainEvent (shared-kernel,
 * Day 2) — eventId, occurredOn, eventType, plus whatever
 * domain-specific fields a test needs — so hand-written JSON strings
 * never leak into individual test classes.
 */
public final class TestEventBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final Map<String, Object> fields = new LinkedHashMap<>();

    private TestEventBuilder(String eventType) {
        fields.put("eventId", UUID.randomUUID().toString());
        fields.put("occurredOn", Instant.now().toString());
        fields.put("eventType", eventType);
        fields.put("eventVersion", 1);
    }

    public static TestEventBuilder of(String eventType) {
        return new TestEventBuilder(eventType);
    }

    public TestEventBuilder with(String field, Object value) {
        fields.put(field, value);
        return this;
    }

    /** Matches the nested {amount, currency} shape used by every Money-carrying event platform-wide. */
    public TestEventBuilder withMoney(String field, String amount, String currency) {
        fields.put(field, Map.of("amount", amount, "currency", currency));
        return this;
    }

    public String toJson() {
        try {
            return MAPPER.writeValueAsString(fields);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize test event", e);
        }
    }
}
