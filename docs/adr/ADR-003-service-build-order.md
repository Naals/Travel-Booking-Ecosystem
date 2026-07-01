# ADR-003: Service Build Order and Staging Strategy

## Status
Accepted

## Context
The platform spec calls for 21 services. Building all 21 simultaneously
to the same depth would produce 21 shallow, low-value implementations
rather than a few production-credible ones. We need an explicit, written
build order so that (a) reviewers can see the prioritization rationale,
and (b) no service is silently dropped or forgotten as work proceeds.

## Decision
All 21 modules are declared in the root `pom.xml` from day one (empty
shells initially), so the build order is structurally visible in the POM
itself, not just in commit messages. Services are implemented in five
tiers:

**Tier 0 — Platform infrastructure (no business logic)**
discovery-server, config-server, api-gateway

**Tier 1 — Identity and core booking saga**
identity-service, booking-service, payment-service, notification-service

**Tier 2 — Inventory and search**
property-service, hotel-service, flight-service, vehicle-service, search-service

**Tier 3 — Engagement and supporting domain**
user-service, review-service, messaging-service, wallet-service, loyalty-service

**Tier 4 — Intelligence and operations**
recommendation-service, fraud-service, analytics-service, audit-service

Each tier's services get a full domain model, persistence layer, tests,
and Docker/K8s manifests before the next tier starts. A module existing
as an empty Maven module in the POM (Tier 2-4 before their turn) is
expected and tracked — it is not the same as a forgotten module.

## Consequences
Easier: progress is auditable — `git log -- <service>/` shows exactly
when each of the 21 services was touched; an empty module in the POM
with no source is a visible placeholder, not a silent gap.
Harder: services in later tiers (e.g. fraud-service, audit-service) will
have no working code for an extended period — this is intentional
sequencing, not neglect, and is documented here so it isn't mistaken for
an oversight.

## Alternatives Considered
- Build all 21 in parallel at low depth — rejected, produces a portfolio
  that looks broad but demonstrates no production-level competency anywhere.
- Don't declare unbuilt modules in the POM until their tier starts —
  rejected, because it makes "did we forget a service" unanswerable by
  inspection; the POM module list is now the single source of truth for
  total scope.
