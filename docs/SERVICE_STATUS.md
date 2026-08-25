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
| 20 | analytics-service | 4 | not started | — |
| 21 | audit-service | 4 | not started | — |

## Shared modules
| Module | Status | Day |
|---|---|---|
| shared-kernel | ✅ core complete | Day 2 |
| common-lib | ✅ core complete | Day 3 (extended Days 15–19, 21) |

## Intentional deferrals (see linked ADRs)
- **search-service's `rating` field** (Day 14, ADR-007): still static.
- **`PaymentMethod.WALLET`** (payment-service, Day 8): unimplemented — ADR-010.
- **MFA** (identity-service, Day 6): domain model exists, no endpoint or live Twilio integration.
- **Redeemed loyalty points have no destination** (loyalty-service, Day 19).
- **FLIGHT bookings excluded from recommendation signals** (Day 20, ADR-012).
- **`FRAUD_CHECK_REQUESTED`** (declared Day 3): still has no producer after fraud-service — ADR-013.

## Closed loops (cross-service integrations completed after being left open)
- **user-service's SavedLocationAddedEvent** (Day 15) → first consumed by recommendation-service (Day 20).
- **wallet-service's WalletFrozenEvent** (Day 18) → first consumed by fraud-service (Day 21), which also gained the ability to *cause* a freeze via a new wallet-service consumer the same day — see ADR-013.

## Tier 4 in progress (2 of 4)
recommendation-service and fraud-service both operational. Two of
Tier 4's four services now demonstrate the platform's event-driven
closed-loop pattern working end-to-end without any synchronous
inter-service call.
