# Service implementation status

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
| 21 | audit-service | 4 | not started | — |

## Shared modules
| Module | Status | Day |
|---|---|---|
| shared-kernel | ✅ core complete | Day 2 (eventId/occurredOn first used Day 22) |
| common-lib | ✅ core complete | Day 3 (extended Days 15–19, 21; ANALYTICS_EVENT retired unused Day 22) |

## Intentional deferrals (see linked ADRs)
- **search-service's `rating` field** (Day 14, ADR-007): still static.
- **`PaymentMethod.WALLET`** (payment-service, Day 8): unimplemented — ADR-010.
- **MFA** (identity-service, Day 6): domain model exists, no endpoint or live Twilio integration.
- **Redeemed loyalty points have no destination** (loyalty-service, Day 19).
- **FLIGHT bookings excluded from recommendation signals** (Day 20, ADR-012).
- **`FRAUD_CHECK_REQUESTED`** (declared Day 3): no producer exists — ADR-013.
- **`ANALYTICS_EVENT`** (declared Day 3): formally retired, no producer will ever exist — ADR-014.
- **`processed_events` has no cleanup job** (analytics-service, Day 22): unbounded growth, flagged — ADR-014.

## Reliability pattern pairs completed
- **Outbox** (ADR-005, Day 9): guarantees a producer's event is eventually published at-least-once.
- **Inbox** (ADR-014, Day 22): guarantees a consumer's effect from that event is applied at-most-once. First real use of `DomainEvent.eventId` (shared-kernel, Day 2) — carried in every event since day one, unused by any consumer until today.

## Tier 4 in progress (3 of 4)
analytics-service is the third consumer-only service (after search,
Day 14, and recommendation, Day 20) and the first platform-wide,
non-per-user Tier 3/4 service — no `identity.user-registered`
consumer. Only `audit-service` remains to close out all 21 services.
