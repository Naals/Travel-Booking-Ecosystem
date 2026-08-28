# ADR-014: Consumer-Side Event Deduplication via the Inbox Pattern

## Status
Accepted

## Context
analytics-service (Day 22) must track revenue and funnel counts
reliably. Kafka's at-least-once delivery means any consumer, in this
platform or any other, can receive the same message more than once.
recommendation-service (ADR-012, Day 20) and fraud-service (ADR-013,
Day 21) both explicitly accepted duplicate-delivery risk as low-stakes
for soft ranking/detection signals. Revenue and funnel numbers feeding
business reporting deserve a stronger guarantee — a double-counted
payment inflates a dashboard in a way a slightly-over-eager fraud
signal does not.

Separately: `KafkaTopics.ANALYTICS_EVENT` was declared on Day 3,
implying a generic topic every producer would forward analytics-
relevant facts to. By Day 22, every service already publishes its own
rich, strongly-typed events (BookingCreatedEvent, PaymentCompletedEvent,
etc.) on dedicated topics. Requiring 21 services to *additionally*
publish to a generic forwarding topic would be pure duplication for no
benefit a direct subscription doesn't already provide.

## Decision
**Retire `ANALYTICS_EVENT`.** analytics-service subscribes directly to
the existing topics it needs (`booking.*`, `payment.payment-completed`,
`payment.refund-completed`). The constant remains declared in
common-lib for historical continuity but is formally unused — the
third instance in this platform of an early Day-3 design decision
being explicitly closed out rather than forced into existence
(alongside `PaymentMethod.WALLET`, ADR-010, and
`FRAUD_CHECK_REQUESTED`, ADR-013).

**Deduplicate using `DomainEvent.eventId`.** Every domain event in this
platform has carried a UUID `eventId` (shared-kernel, Day 2) since the
very first aggregate — visible in every event's JSON payload via
Jackson's default getter serialization, silently ignored by every
consumer until today. analytics-service is the first to use it for its
intended purpose: before applying an event's effect, each consumer
attempts `INSERT INTO processed_events (event_id, ...) VALUES (...)
ON CONFLICT (event_id) DO NOTHING`; a duplicate delivery finds the row
already present, the insert affects zero rows, and the handler skips
straight to acknowledging. This is a third distinct idempotency
mechanism in this platform — after MongoDB's `findAndModify`
(review-service, Day 16) and a conditional `UPDATE`
(loyalty-service, Day 19) — each chosen as the natural fit for its own
datastore rather than forcing one mechanism everywhere.

The dedup check and the metric increment run inside the same
`@Transactional` boundary (placed on the public `@KafkaListener`
method itself, not the private `handle()` helper it calls, to avoid
Spring's well-known self-invocation gotcha bypassing the transactional
proxy). A genuine processing failure rolls both back together, so
retried delivery is still handled correctly; only a crash after commit
but before the Kafka offset is acknowledged could theoretically still
double-process, the same residual risk every at-least-once system
lives with.

**This completes the reliability pattern pair.** The Outbox Pattern
(ADR-005, Day 9) guarantees a producer's event is eventually published
at-least-once. The Inbox Pattern (this ADR) guarantees a consumer's
effect from that event is applied at-most-once. Combined: effectively-
once processing, without requiring Kafka exactly-once transactional
semantics anywhere in the platform.

**`occurredOn` drives date-bucketing**, not local processing-time —
also a shared-kernel Day 2 field, previously used only for its own
value, never for date arithmetic by any consumer. Using event time
instead of processing time avoids skewing a metric into the wrong
calendar day if consumer lag pushes real-time processing past
midnight. Parses correctly as ISO-8601 because of config-server's
shared Jackson setting (`write-dates-as-timestamps: false`, Day 4).

**A third instance of the local-lookup-bridging-two-events shape.**
`BookingConfirmedEvent` and `BookingCancelledEvent` (Day 7) carry no
`bookingType` field — only `BookingCreatedEvent` and the Day-15-
enriched `BookingCompletedEvent` do. `BookingTypeLookupRepository`
bridges this the same way `SpendRecord` (loyalty-service, Day 19) and
`DestinationLookup` (recommendation-service, Day 20) did for their own
gaps — a recognized, deliberate pattern in this platform, not a fresh
workaround invented each time it recurs.

## Consequences
Easier: revenue and funnel numbers stay correct under Kafka
redelivery without platform-wide exactly-once infrastructure; the
dedup mechanism is generic enough that any future consumer needing
this same guarantee can reuse the identical `processed_events` table
shape.

Harder: `processed_events` grows without bound — no cleanup job ships
today, flagged as future work once the platform's real redelivery
window (how far back duplicates can realistically arrive) is
understood well enough to size a retention period safely. A true
out-of-order delivery between `BOOKING_CREATED` and
`BOOKING_CONFIRMED`/`BOOKING_CANCELLED` (different topics, no cross-
topic ordering guarantee) can still cause a confirmed/cancelled count
to be dropped rather than mis-bucketed — logged as a warning, not
guessed at, the same choice made throughout this platform whenever a
genuine gap is discovered rather than silently patched over.

## Alternatives Considered
- Kafka exactly-once semantics (transactional producers/consumers)
  platform-wide — rejected as a sweeping infrastructure change
  touching all 21 services for a guarantee only this one currently
  needs.
- Keep `ANALYTICS_EVENT` and require every producer to also publish to
  it — rejected as pure duplication; every producer already emits a
  richer, more specific event that direct subscription can consume
  without any forwarding layer.
