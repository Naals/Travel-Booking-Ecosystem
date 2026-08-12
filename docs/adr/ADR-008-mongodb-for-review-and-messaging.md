# ADR-008: Polyglot Persistence — MongoDB for Review and Messaging Services

## Status
Accepted

## Context
Every service through Day 15 uses PostgreSQL, even where the domain
doesn't cleanly fit a relational shape. review-service (this day) and
messaging-service (Day 17) are the first two candidates for a document
store instead: both write mostly self-contained, independently-read
documents (a review; a chat message) with few cross-document joins,
and both need to handle high-volume, append-heavy, loosely-structured
content — the pattern the original spec explicitly calls out MongoDB
for (Reviews, Messages).

## Decision
Use MongoDB (Spring Data MongoDB) for review-service and
messaging-service. Each gets its own logical database (review_db,
messaging_db) on a single shared MongoDB server — unlike PostgreSQL,
which got one dedicated container per service in docker-compose.yml.
This is a deliberate divergence, not an inconsistency: the
one-container-per-service pattern emerged because Postgres services
were built one at a time across many days, each needing its own
immediately-available port; both Mongo services are introduced within
the same tier and can share one server with two logical databases from
day one — simpler to run locally while still preserving per-service
data ownership (no cross-database queries, no service reaching into
another's collections).

No Flyway equivalent: indexes are declared via @Indexed /
@CompoundIndex on the document classes and created automatically on
startup (spring.data.mongodb.auto-index-creation), rather than a
versioned SQL file.

No @Transactional the way PostgreSQL services use it: MongoDB
single-document writes are atomic without any special handling, and
the one place review-service needs an atomic check-and-set across a
race — marking a booking's review eligibility as consumed — uses
MongoDB's native findAndModify rather than a multi-document
transaction, which would require a replica-set deployment not run
locally. See ReviewEligibilityRepositoryAdapter.tryConsume().

## Consequences
Easier: adding a field to a review or message document needs no
migration file — extend the domain model and document class, and old
documents simply lack the field until rewritten. Horizontal read
scaling for review listing pages is simpler to reason about than the
equivalent PostgreSQL read-replica setup.

Harder: no migration history to review in a PR — an accidental
unindexed query pattern is a runtime performance regression, not a
review-time diff. Cross-collection consistency (Review, RatingSummary,
ReviewEligibility all live in separate collections) is entirely the
application's responsibility; nothing enforces referential integrity
the way a PostgreSQL foreign key would — user-service's travel_history
table (Day 15) made the same call for the same reason at the
PostgreSQL level, doubly so here where MongoDB has no FK concept at all.

**Known gap, deliberately left open:** search-service's SearchDocument
(Day 14, ADR-007) has a `rating` field populated today only by
hotel-service's static star rating and never updated from actual
review activity. ResourceRatingUpdatedEvent (this day) is the natural
signal search-service would consume to close that gap, but wiring a
new consumer into an already-shipped service is left for a future,
dedicated pass rather than folded into this day's scope — consistent
with how SavedLocationAddedEvent (Day 15) was published with no
consumer yet.

## Alternatives Considered
- Stay on PostgreSQL with JSONB columns for the loosely-structured
  fields — rejected: the original spec explicitly calls for polyglot
  persistence as a demonstrated skill, and JSONB-in-Postgres would
  forgo genuine competency with a document database's query and
  indexing model.
- One dedicated MongoDB container per service, mirroring the Postgres
  pattern — rejected for now (see Decision). Revisit if MongoDB write
  volume ever becomes a genuine capacity bottleneck for either service.
