# ADR-012: Heuristic Destination Recommendations Built from Local Event Projections

## Status
Accepted

## Context
recommendation-service (Day 20, first Tier 4 service) must implement
"personalized recommendations" and "trending destinations" per the
original spec. Two significant design forks exist: (1) heuristic
scoring vs. a real ML/collaborative-filtering pipeline, and (2) how to
learn "which city is this booking in" without a synchronous call to
the four Tier 2 inventory services, none of which recommendation-service
owns data for.

## Decision
**Heuristic, not ML.** Recommendations are a weighted combination of
two signals: a per-user affinity score (a saved location = weight 3, a
completed booking resolved to a destination = weight 10) and a global
trending count (completed bookings per destination, unweighted). No
collaborative filtering, no embeddings, no external ML service. The
same "correct, simple, explicitly-scoped" choice PointsCalculationPolicy
and TierCalculationPolicy made for loyalty-service (Day 19) — a real,
defensible algorithm, not a placeholder pretending to be more
sophisticated than it is.

**Local projections, not synchronous calls.** To resolve "this booking
was for property X" into "that's in Istanbul, TR," recommendation-service
consumes PropertyCreatedEvent, HotelCreatedEvent, and
VehicleAddedToFleetEvent (already published to the `search.index-*`
topics for search-service, Day 14) into a local DestinationLookup
correlation table, keyed the same way review-service's
BookingEventConsumer (Day 16) already parses resourceId per booking
type. Consistent with every prior decision ruling out synchronous
inter-service calls (ADR-001, ADR-010).

**Known gap: FLIGHT bookings are excluded.** FlightScheduledEvent
(flight-service, Day 12) carries only IATA airport codes
(originCode/destinationCode), never a city or country name — unlike
the other three inventory-created events, which all carry city and
country directly. Resolving IATA codes to destination names would need
either a static code-to-city mapping (a new, unowned data set) or a
synchronous lookup (ruled out above). Flight bookings therefore
contribute no affinity or popularity signal in this version —
BookingCompletedEvent with bookingType=FLIGHT resolves to no
DestinationLookup entry and is logged and skipped. Documented here
rather than worked around with a guess, the same treatment ADR-007
gave to missing price/geo fields on inventory-created events.

**No idempotency guard on signal recording.** Unlike wallet-service's
WalletTransaction (Day 18) or loyalty-service's SpendRecord (Day 19),
recording an affinity or popularity signal has no duplicate-delivery
guard. A redelivered BookingCompletedEvent would double-count a single
trip. Accepted deliberately: these are soft, self-correcting ranking
signals, not a financial ledger or a points balance — the cost of an
occasional duplicate is negligible next to the engineering cost of a
`tryConsume()`-style guard (loyalty-service, Day 19) for every signal
path. Worth revisiting if it ever demonstrably skews rankings.

**Destination-level, not listing-level.** Recommendations return
`(city, country, score)`, not specific bookable properties or hotels.
The intended client flow is: call `/api/v1/recommendations/me`, then
call search-service's existing `/api/v1/search?city=...` (Day 14) for
actual inventory in a recommended destination. Keeping the two
concerns separate avoids recommendation-service duplicating
search-service's inventory knowledge.

## Consequences
Easier: the entire scoring model is two small, pure domain classes
(UserAffinity, DestinationPopularity) and one static ranking function
(RecommendationEngine) — auditable and unit-testable without any
external ML infrastructure. New users see trending-only results by
default, a correct and expected cold-start behavior requiring no
special-casing.

Harder: recommendation quality is coarse — no recency decay (a signal
from a year ago counts the same as one from yesterday), no negative
signals, and flights are invisible to the whole system until a
canonical airport-to-city mapping exists somewhere in the platform.

**Naming debt inherited, not fixed:** subscribing to
`search.index-property`, `search.index-hotel`, and `search.index-vehicle`
for a fact that is really "this inventory item now exists," not
specifically "index this for search," is a mild misnomer this service
now depends on too. Renaming an established, already-consumed-elsewhere
topic this late carries more risk than the naming issue itself — left
as-is, noted here rather than silently inherited.

## Alternatives Considered
- A shared `resourceId → destination` lookup owned by a new or existing
  service, queried synchronously — rejected for the same reason every
  cross-service synchronous call has been rejected in this platform
  since ADR-001.
- Recency-weighted decay on affinity scores — rejected for v1 as an
  easy, well-understood future enhancement (multiply weight by a
  half-life function of `lastSignalAt`) not worth the complexity before
  the simpler version has proven useful.
- A static IATA-code-to-city reference table to unblock flight
  destinations — genuinely viable future work, deliberately not built
  today; it is a real data-maintenance commitment (airport codes
  change, new airports open) that deserves its own decision, not a
  rushed addition to close a gap noticed mid-day.
