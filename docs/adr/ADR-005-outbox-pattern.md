# ADR-005: Outbox Pattern for Reliable Kafka Publishing

## Status
Accepted

## Context
Every domain service needs to publish Kafka events after persisting
aggregate state. The naive approach — save to DB, then publish to Kafka
— creates a dual-write problem: the DB write could succeed while the
Kafka publish fails, leaving the system in an inconsistent state.
The reverse (publish first, then save) has the same problem.

## Decision
Use the Transactional Outbox Pattern on identity-service (Day 6) as
the reference implementation. All Tier 1 services that need reliable
event publishing follow the same pattern:

1. Domain event is written to an outbox table in the SAME transaction
   as the aggregate state change.
2. A scheduled poller (OutboxEventPoller) reads unprocessed outbox rows
   and publishes them to Kafka.
3. On successful publish, the row is marked as processed.
4. If the JVM dies between publish and markAsProcessed, the event is
   republished on the next poll — consumers must be idempotent.

booking-service and payment-service publish directly to Kafka (no outbox)
because their events are part of an already-complex saga and the
coordinator is stateless — a failed publish causes the consumer to not
ack, which triggers redelivery. notification-service is a pure consumer
and has no outbox concern.

## Consequences
Easier: guaranteed at-least-once delivery; no dual-write window; easy
to inspect pending events in the DB for debugging.
Harder: slight publish latency (poller interval = 1 second); requires
an outbox table per service that uses it; consumers must deduplicate.

## Alternatives Considered
- Kafka Transactions — rejected, requires exactly-once semantics config
  across all consumers which adds operational complexity.
- Debezium CDC — better for production (lower latency, no polling), but
  requires a running Debezium connector and Kafka Connect cluster. Noted
  as the production upgrade path from the polling approach used here.
