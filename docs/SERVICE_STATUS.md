# Service implementation status

| # | Service | Tier | Status | Day |
|---|---|---|---|---|
| 1 | discovery-server | 0 | ✅ core complete | Day 4 |
| 2 | config-server | 0 | ✅ core complete | Day 4 |
| 3 | api-gateway | 0 | ✅ core complete | Day 5 |
| 4 | identity-service | 1 | ✅ core complete | Day 6 |
| 5 | booking-service | 1 | ✅ core complete | Day 7 (event enriched Day 15) |
| 6 | payment-service | 1 | ✅ core complete | Day 8 |
| 7 | notification-service | 1 | ✅ core complete | Day 9 (extended Day 17) |
| 8 | property-service | 2 | ✅ core complete | Day 10 |
| 9 | hotel-service | 2 | ✅ core complete | Day 11 |
| 10 | flight-service | 2 | ✅ core complete | Day 12 |
| 11 | vehicle-service | 2 | ✅ core complete | Day 13 |
| 12 | search-service | 2 | ✅ core complete | Day 14 |
| 13 | user-service | 3 | ✅ core complete | Day 15 |
| 14 | review-service | 3 | ✅ core complete | Day 16 |
| 15 | messaging-service | 3 | ✅ core complete | Day 17 |
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
| common-lib | ✅ core complete | Day 3 (extended Days 15–17) |

## Known issues (tech debt)
- **Stale `createdAt`/`updatedAt` on reconstitution**: Booking (Day 7),
  Property (Day 10), Hotel (Day 11), Flight (Day 12), Vehicle (Day 13),
  UserProfile (Day 15), and Review (Day 16) all unconditionally set
  `createdAt = Instant.now()` in their private constructors;
  `reconstitute()` accepts `createdAt`/`updatedAt` parameters but never
  applies them. Net effect: re-fetching any of these aggregates reports
  `createdAt` as "now" rather than the true original creation time in
  API responses. Not a data-loss bug — the correct value is stored
  correctly in each database — a read-path bug in the domain layer
  only. Caught while building messaging-service (Day 17), whose
  `Conversation`/`Message` constructors take `createdAt`/`sentAt`
  explicitly to avoid it. Back-porting the fix to the services above is
  scoped as a future hardening pass, not folded into Day 17.

## Tier 3 in progress (3 of 5)
messaging-service is the platform's second MongoDB-backed service and
the first with zero Kafka consumers of its own — see its application
class Javadoc. `MessageSentEvent` reuses `KafkaTopics.MESSAGE_SENT`,
declared in common-lib since Day 3 and unused until now.
