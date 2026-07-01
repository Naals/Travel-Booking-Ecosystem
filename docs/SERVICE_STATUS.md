# Service implementation status

Single source of truth for "has this service been started yet."
Updated in the same commit whenever a service moves between states.

| # | Service | Tier | Status | First commit |
|---|---|---|---|---|
| 1 | discovery-server | 0 | not started | — |
| 2 | config-server | 0 | not started | — |
| 3 | api-gateway | 0 | not started | — |
| 4 | identity-service | 1 | not started | — |
| 5 | booking-service | 1 | not started | — |
| 6 | payment-service | 1 | not started | — |
| 7 | notification-service | 1 | not started | — |
| 8 | property-service | 2 | not started | — |
| 9 | hotel-service | 2 | not started | — |
| 10 | flight-service | 2 | not started | — |
| 11 | vehicle-service | 2 | not started | — |
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

Statuses: `not started` → `scaffolded` (POM + dirs, no logic) →
`in progress` → `core complete` (domain + tests passing).
