# ADR-007: Unified Search Index Design

## Status
Accepted

## Context
Four Tier 2 services (property, hotel, flight, vehicle) each own
fundamentally different domain models, but customers expect a single
"search everything" experience. Search needs to be fast, support
full-text keyword matching, numeric range filters, geo-distance, and
sorting — capabilities PostgreSQL is not well-suited for at scale.

Additionally: the "listing created" events these four services
currently publish (PropertyCreatedEvent, HotelCreatedEvent,
FlightScheduledEvent, VehicleAddedToFleetEvent — see Days 10-13) were
designed minimally, carrying identity and location fields but not
price or geo-coordinates. This is a real, common consequence of
building event schemas incrementally rather than a design flaw to
paper over.

## Decision
**Single Elasticsearch index ("listings"), discriminated by a
`listingType` keyword field**, rather than four separate per-type
indices. All four inventory services publish to their existing
`search.index-*` topics (already defined in common-lib's KafkaTopics
since Day 3); search-service consumes all four into one index.

**CQRS read model**: SearchDocument is explicitly not an aggregate
root. It enforces only basic construction validity, not business
invariants — the source of truth remains each owning service's
aggregate. This index can be rebuilt from scratch by replaying Kafka
history if it's ever lost or needs a mapping change.

**Availability signal handling**: PropertyAvailabilityUpdated,
RoomInventoryUpdated, and SeatInventoryUpdated events carry only an
ID — the same event fires whether a hold was placed or released, so
this service cannot infer new state from the event alone. Rather than
guess, `touchAvailabilitySignal()` records that *something* changed
(via a `lastAvailabilityEventAt` timestamp) without asserting a new
`available` value. `flight.status-changed` is the one exception,
since it carries an explicit `newStatus` field — a transition to
CANCELLED is handled with a firm `markUnavailable()` call.

**Missing price/geo data**: priceAmount and location are nullable
fields, populated only where the current upstream events provide
them (which today is: nowhere, except geo/price fields are ready in
schema for when producer events are enriched).

## Consequences
Easier: one query surface for the entire platform; adding a fifth
searchable type is "add a Kafka consumer + a listingType value," not
a new index or new query API; index can be dropped and rebuilt from
Kafka history without touching the owning services.

Harder: price and geo-distance filters will not meaningfully narrow
results until the producer event schemas are enriched — documented
here rather than silently degraded; a single large index means all
four types share sharding/replica settings, which may not be optimal
per-type at very large scale (mitigated: `listingType` filter uses an
inverted index lookup, cost is similar to a routing-based approach at
this scale).

## Production upgrade path
Two options, not mutually exclusive:
1. Enrich PropertyCreatedEvent, HotelCreatedEvent, etc. with price
   and coordinates directly (schema version bump on each event).
2. Have search-service call back to the owning service's REST API on
   receipt of a thin event, to fetch and backfill the full listing
   projection (classic CQRS "event as pointer, not payload" pattern).

## Alternatives Considered
- Four separate indices (properties, hotels, flights, vehicles) with
  a fan-out query at search time — rejected for this stage; adds
  query-time complexity (scatter-gather + result merging/re-ranking)
  for a benefit (per-type mapping tuning) that isn't yet needed at
  portfolio scale.
- Search-service owning a local read replica of each PostgreSQL DB via
  CDC (Debezium) instead of consuming lightweight events — rejected as
  premature; the current event-driven approach is simpler to operate
  and sufficient until data volume justifies CDC.
