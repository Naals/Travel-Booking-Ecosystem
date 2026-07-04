package com.travel.shared.domain;

import com.travel.shared.event.DomainEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

@DisplayName("AggregateRoot")
class AggregateRootTest {

    static class SomethingHappenedEvent extends DomainEvent {
        private final String id;
        SomethingHappenedEvent(String id) { super("SomethingHappened"); this.id = id; }
        @Override public String getAggregateId() { return id; }
    }

    static class TestAggregate extends AggregateRoot<String> {
        TestAggregate(String id) { super(id); }
        void doSomething() { registerEvent(new SomethingHappenedEvent(getId())); }
    }

    TestAggregate aggregate;

    @BeforeEach
    void setUp() {
        aggregate = new TestAggregate("agg-1");
    }

    @Nested
    @DisplayName("Event accumulation")
    class EventAccumulation {

        @Test
        @DisplayName("starts with no events")
        void noEventsInitially() {
            assertThat(aggregate.getDomainEvents()).isEmpty();
        }

        @Test
        @DisplayName("accumulates events from state changes")
        void accumulatesEvents() {
            aggregate.doSomething();
            aggregate.doSomething();
            assertThat(aggregate.getDomainEvents()).hasSize(2);
        }

        @Test
        @DisplayName("clears events after outbox write")
        void clearsEvents() {
            aggregate.doSomething();
            aggregate.clearDomainEvents();
            assertThat(aggregate.getDomainEvents()).isEmpty();
        }
    }

    @Nested
    @DisplayName("Immutability guard")
    class ImmutabilityGuard {

        @Test
        @DisplayName("getDomainEvents returns unmodifiable list")
        void eventsListIsUnmodifiable() {
            aggregate.doSomething();
            var events = aggregate.getDomainEvents();
            assertThatThrownBy(() -> events.add(new SomethingHappenedEvent("x")))
                .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("Event content")
    class EventContent {

        @Test
        @DisplayName("registered event carries correct aggregateId")
        void eventAggregateId() {
            aggregate.doSomething();
            var event = aggregate.getDomainEvents().get(0);
            assertThat(event.getAggregateId()).isEqualTo("agg-1");
            assertThat(event.getEventType()).isEqualTo("SomethingHappened");
        }

        @Test
        @DisplayName("each event has a unique eventId")
        void uniqueEventIds() {
            aggregate.doSomething();
            aggregate.doSomething();
            var ids = aggregate.getDomainEvents()
                .stream()
                .map(DomainEvent::getEventId)
                .toList();
            assertThat(ids).doesNotHaveDuplicates();
        }

        @Test
        @DisplayName("event occurredOn is set at construction")
        void occurredOnIsSet() {
            aggregate.doSomething();
            assertThat(aggregate.getDomainEvents().get(0).getOccurredOn()).isNotNull();
        }
    }
}
