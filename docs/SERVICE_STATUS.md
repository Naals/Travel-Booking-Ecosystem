# Service implementation status

## ✅ Platform status: all 21 services core-complete (Day 23)

| # | Service | Tier | Status | Day |
|---|---|---|---|---|
| 1 | discovery-server | 0 | ✅ core complete | Day 4 |
| 2 | config-server | 0 | ✅ core complete | Day 4 |
| 3 | api-gateway | 0 | ✅ core complete | Day 5 |
| 4 | identity-service | 1 | ✅ core complete | Day 6 |
| 5 | booking-service | 1 | ✅ core complete | Day 7 (event enriched Day 15) |
| 6 | payment-service | 1 | ✅ core complete | Day 8 |
| 7 | notification-service | 1 | ✅ core complete | Day 9 (extended Days 17, 19) |
| 8 | property-service | 2 | ✅ core complete | Day 10 |
| 9 | hotel-service | 2 | ✅ core complete | Day 11 |
| 10 | flight-service | 2 | ✅ core complete | Day 12 |
| 11 | vehicle-service | 2 | ✅ core complete | Day 13 |
| 12 | search-service | 2 | ✅ core complete | Day 14 |
| 13 | user-service | 3 | ✅ core complete | Day 15 |
| 14 | review-service | 3 | ✅ core complete | Day 16 |
| 15 | messaging-service | 3 | ✅ core complete | Day 17 |
| 16 | wallet-service | 3 | ✅ core complete | Day 18 (consumer added Day 21) |
| 17 | loyalty-service | 3 | ✅ core complete | Day 19 |
| 18 | recommendation-service | 4 | ✅ core complete | Day 20 |
| 19 | fraud-service | 4 | ✅ core complete | Day 21 |
| 20 | analytics-service | 4 | ✅ core complete | Day 22 |
| 21 | audit-service | 4 | ✅ core complete | Day 23 |

## Shared modules
| Module | Status | Day |
|---|---|---|
| shared-kernel | ✅ core complete | Day 2 (Entity used standalone, without AggregateRoot, for the first time Day 23) |
| common-lib | ✅ core complete | Day 3 (every topic declared that day now has a producer; ANALYTICS_EVENT formally retired unused, Day 22) |

## Intentional deferrals (see linked ADRs) — carried forward, not silently dropped
- **search-service's `rating` field** (Day 14, ADR-007): still static, not wired to review-service's ResourceRatingUpdatedEvent.
- **`PaymentMethod.WALLET`** (payment-service, Day 8): unimplemented — ADR-010.
- **MFA** (identity-service, Day 6): domain model exists, no endpoint or live Twilio integration.
- **Redeemed loyalty points have no destination** (loyalty-service, Day 19): ledger-only.
- **FLIGHT bookings excluded from recommendation signals** (Day 20, ADR-012).
- **`FRAUD_CHECK_REQUESTED`** (declared Day 3): no producer exists — ADR-013.
- **`processed_events` has no cleanup job** (analytics-service, Day 22): unbounded growth, flagged — ADR-014.

## Reliability pattern pairs completed
- **Outbox** (ADR-005, Day 9) — a producer's event is eventually published at-least-once.
- **Inbox** (ADR-014, Day 22) — a consumer's effect is applied at-most-once, via `DomainEvent.eventId`.
- **Hash chain** (ADR-015, Day 23) — a compliance record, once written, is verifiably tamper-evident.

## Closed loops (cross-service integrations completed after being left open)
- **user-service's SavedLocationAddedEvent** (Day 15) → consumed by recommendation-service (Day 20).
- **wallet-service's WalletFrozenEvent** (Day 18) → consumed by fraud-service (Day 21), which also gained the ability to *cause* a freeze.

## Remaining work to close out the full portfolio
Core service implementation (domain layer, persistence, Kafka
integration, REST API, unit tests, Dockerfile) is complete for all 21
services. Not yet done, and explicitly tracked here rather than
implied:
- **Integration testing** — Testcontainers dependencies are declared
  throughout, but no end-to-end integration test suite exercising the
  real saga across real containers has been written.
- **Kubernetes manifests** — `infrastructure/kubernetes/` covers Tier
  0-1 services (Day 4-9 era); Tier 2-4 services need their own
  Deployment/Service/ConfigMap entries.
- **CI/CD pipeline** — `.github/workflows/` scaffolding exists from
  Day 1; no working GitHub Actions pipeline has actually been built
  against this many-module Maven project.
- **README / documentation layer** — root `README.md`'s module table
  needs to grow from its Day-1 partial list to all 21 services; a C4
  diagram set (`docs/c4/`) was scaffolded Day 1 and never populated.

These are the natural next days of work, not gaps discovered late —
consistent with how every deferral in this platform has been tracked
throughout: named, dated, and left for a deliberate follow-up pass
rather than either rushed in or hidden.
