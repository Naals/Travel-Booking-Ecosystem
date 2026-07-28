# Service implementation status

| # | Service | Tier | Status | Day |
|---|---|---|---|---|
| 1 | discovery-server | 0 | ✅ core complete | Day 4 |
| 2 | config-server | 0 | ✅ core complete | Day 4 |
| 3 | api-gateway | 0 | ✅ core complete | Day 5 |
| 4 | identity-service | 1 | ✅ core complete | Day 6 |
| 5 | booking-service | 1 | ✅ core complete | Day 7 |
| 6 | payment-service | 1 | ✅ core complete | Day 8 |
| 7 | notification-service | 1 | ✅ core complete | Day 9 |
| 8 | property-service | 2 | ✅ core complete | Day 10 |
| 9 | hotel-service | 2 | ✅ core complete | Day 11 |
| 10 | flight-service | 2 | ✅ core complete | Day 12 |
| 11 | vehicle-service | 2 | ✅ core complete | Day 13 |
| 12 | search-service | 2 | not started | — |
| 13 | user-service | 3 | not started | — |
| 14 | review-service | 3 | not started | — |
| 15 | messaging-service | 3 | not started | — |
| 16 | wallet-service | 3 | not started | — |
| 17 | loyalty-service | 3 | not started | — |
| 18 | recommendation-service | 4 | not started | — |
| 19 | fraud-service | 4 | not started | — |
| 20 | analytics-service | 4 | not started | — |
| 21 | audit-service | 4 | not started | — |

## Shared modules
| Module | Status | Day |
|---|---|---|
| shared-kernel | ✅ core complete | Day 2 |
| common-lib | ✅ core complete | Day 3 |

## Tier 2 inventory complete ✅
All four inventory services participate correctly in the saga:
property, hotel, flight, and vehicle each consume BookingCreated,
reserve their respective resource, and publish back to the saga.
ADR-006 documents the inventory service patterns and resourceId
encoding convention.
