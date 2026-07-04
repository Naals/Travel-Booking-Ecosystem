package com.travel.shared.domain;

import com.travel.shared.event.DomainEvent;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Base class for all DDD Aggregate Roots in the travel platform.
 *
 * An aggregate root is the single entry point to a cluster of domain objects.
 * It enforces all invariants for the cluster and is the only object that
 * external code holds a direct reference to.
 *
 * Domain event lifecycle (Outbox Pattern):
 *   1. A state-changing method calls registerEvent(event)
 *   2. The aggregate is persisted inside a transaction
 *   3. The application layer reads getDomainEvents() after save
 *   4. Events are written to the outbox table in the same transaction
 *   5. A scheduler publishes outbox entries to Kafka asynchronously
 *   6. clearDomainEvents() is called once publishing is confirmed
 *
 * This guarantees at-least-once delivery without a dual-write problem.
 *
 * @param <ID> the type of the aggregate's identity value object
 */
public abstract class AggregateRoot<ID> extends Entity<ID> {

    private final transient List<DomainEvent> domainEvents = new ArrayList<>();

    protected AggregateRoot(ID id) {
        super(id);
    }

    /**
     * Registers a domain event to be published after the aggregate persists.
     * Call from within state-changing domain methods only.
     */
    protected void registerEvent(DomainEvent event) {
        domainEvents.add(event);
    }

    /**
     * Returns an unmodifiable snapshot of accumulated events.
     * Read by the application layer after save() to feed the outbox.
     */
    public List<DomainEvent> getDomainEvents() {
        return Collections.unmodifiableList(domainEvents);
    }

    /**
     * Clears all accumulated events.
     * Called by the application layer after events have been written
     * to the outbox table.
     */
    public void clearDomainEvents() {
        domainEvents.clear();
    }
}
