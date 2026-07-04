package com.travel.shared.event;

import java.time.Instant;
import java.util.UUID;

/**
 * Base class for all domain events in the travel platform.
 *
 * Domain events represent facts that happened inside a bounded context.
 * They are immutable, past-tense records used for:
 *
 *   - Decoupling bounded contexts via Kafka (async choreography)
 *   - Driving the Saga pattern for distributed transactions
 *     (BookingCreated → InventoryReserved → PaymentCompleted → Confirmed)
 *   - Feeding CQRS read-model projections (search indexing, analytics)
 *   - Reliable publishing via the Outbox Pattern
 *
 * getAggregateId() is used as the Kafka partition key so all events for
 * the same aggregate are processed in order.
 */
public abstract class DomainEvent {

    private final String eventId;
    private final Instant occurredOn;
    private final String eventType;
    private final int eventVersion;

    protected DomainEvent(String eventType) {
        this(eventType, 1);
    }

    protected DomainEvent(String eventType, int eventVersion) {
        this.eventId      = UUID.randomUUID().toString();
        this.occurredOn   = Instant.now();
        this.eventType    = eventType;
        this.eventVersion = eventVersion;
    }

    /**
     * ID of the aggregate that produced this event.
     * Used as Kafka partition key — guarantees ordering per aggregate.
     */
    public abstract String getAggregateId();

    public String getEventId()      { return eventId; }
    public Instant getOccurredOn()  { return occurredOn; }
    public String getEventType()    { return eventType; }
    public int getEventVersion()    { return eventVersion; }
}
