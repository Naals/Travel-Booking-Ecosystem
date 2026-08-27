package com.travel.analytics.domain.repository;

/**
 * A technical/infrastructure concern (Kafka at-least-once redelivery)
 * modeled as a domain-layer port anyway, so the application layer
 * depends only on an abstraction — keeping the hexagonal boundary
 * intact even though this port represents no business concept.
 *
 * See ADR-014: the first consumer in this platform to use
 * DomainEvent.eventId (shared-kernel, Day 2) for deduplication —
 * the Inbox Pattern, completing the Outbox Pattern (ADR-005, Day 9).
 */
public interface EventDeduplicationRepository {

    /**
     * Atomically marks eventId as processed.
     * @return true if this is the first time seeing eventId (safe to
     *         proceed); false if it was already processed (duplicate —
     *         skip).
     */
    boolean markProcessedIfNew(String eventId);
}
