package com.travel.shared.event;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("DomainEvent")
class DomainEventTest {

    static class TestEvent extends DomainEvent {
        TestEvent() { super("TestEvent"); }
        TestEvent(int version) { super("TestEvent", version); }
        @Override public String getAggregateId() { return "agg-1"; }
    }

    @Test
    @DisplayName("eventId is unique per instance")
    void uniqueEventId() {
        var a = new TestEvent();
        var b = new TestEvent();
        assertThat(a.getEventId()).isNotEqualTo(b.getEventId());
    }

    @Test
    @DisplayName("eventType is set from constructor")
    void eventType() {
        assertThat(new TestEvent().getEventType()).isEqualTo("TestEvent");
    }

    @Test
    @DisplayName("default version is 1")
    void defaultVersion() {
        assertThat(new TestEvent().getEventVersion()).isEqualTo(1);
    }

    @Test
    @DisplayName("explicit version is stored correctly")
    void explicitVersion() {
        assertThat(new TestEvent(2).getEventVersion()).isEqualTo(2);
    }

    @Test
    @DisplayName("occurredOn is set at construction and not null")
    void occurredOn() {
        assertThat(new TestEvent().getOccurredOn()).isNotNull();
    }
}
